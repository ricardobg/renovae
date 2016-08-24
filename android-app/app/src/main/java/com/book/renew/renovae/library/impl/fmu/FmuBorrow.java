package com.book.renew.renovae.library.impl.fmu;

import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.Book;
import com.book.renew.renovae.library.impl.BorrowState;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.util.web.Page;
import com.book.renew.renovae.util.web.UrlParser;

import org.jsoup.select.Elements;

import java.util.Date;
import java.util.regex.Matcher;

/**
 * Created by ricardo on 29/07/16.
 */
public class FmuBorrow extends IBorrow {
    private final String _docNumber;
    private final String _itemSequence;
    private String _borrowLink;
    private int _maxRenovations = 5;

    public FmuBorrow(Book book, Date due_date, String borrowLink) {
        super(book, due_date);
        this._borrowLink = borrowLink;

        UrlParser parser = new UrlParser(borrowLink);
        this._docNumber = parser.get("doc_number");
        this._itemSequence = parser.get("item_sequence");
    }

    public void updateDueDate(Date newDueDate) {
        _dueDate = newDueDate;
        _state.incrementRenovations();

    }

    public String getDocNumber() {
        return _docNumber;
    }

    public String getItemSequence() {
        return _itemSequence;
    }

    public void loadBorrowState()
            throws NetworkException, UnexpectedPageContentException, LogoutException {
        Page borrow_page = new Page(_borrowLink);

        FmuUtils.checkLogout(borrow_page);

        Elements borrows_tr = borrow_page.getDoc().select("table:nth-last-of-type(2) > tbody > tr");
        if (borrows_tr.size() < 3)
            throw new UnexpectedPageContentException();
        _borrowDate = FmuUtils.tryParse(borrows_tr.eq(0).select("td").eq(1).text());
        Matcher matcher = FmuUtils.FIND_RENOVATIONS.matcher(borrows_tr.eq(4).text());
        if (!matcher.matches())
            throw new UnexpectedPageContentException();

        int renovations = Integer.parseInt(matcher.group(1));
        _maxRenovations = Integer.parseInt(matcher.group(2));

        Elements renew_a = borrows_tr.eq(2).select("td:eq(1) a");
        if (renew_a.isEmpty()) {
            //It is empty, can't renew
            String msg = borrows_tr.eq(2).select("td:eq(1)").text();
            if (msg != null && !msg.equals("")) {
                if (msg.indexOf("Limite de renov") != -1) {
                    _state = new BorrowState(false, 5,
                            new BorrowState.Cause(BorrowState.Cause.CantRenewCause.MAX_RENEWS,
                                    "Máximo de renovações alcançado: 5/5"));
                }
                else {
                    _state = new BorrowState(false, renovations,
                            new BorrowState.Cause(BorrowState.Cause.CantRenewCause.OVERDUE));
                }

            }
            else
                _state = new BorrowState(false, renovations,
                        new BorrowState.Cause(BorrowState.Cause.CantRenewCause.UNKNOWN));
        }
        else {
            //Can renew
            _state = new BorrowState(true, renovations);
        }
    }

    @Override
    public int getMaxRenews() {
        return _maxRenovations;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof FmuBorrow))
            return false;
        FmuBorrow cmp = (FmuBorrow) obj;
        if (cmp == this)
            return true;
        return (cmp._docNumber == this._docNumber &&
                cmp._itemSequence == this._itemSequence);
    }

}
