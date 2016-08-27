package com.book.renew.renovae.library.impl.fmu;

import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.impl.Book;
import com.book.renew.renovae.library.exception.renew.CantRenewCause;
import com.book.renew.renovae.library.exception.renew.RenewException;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.impl.ILibrary;
import com.book.renew.renovae.util.Util;
import com.book.renew.renovae.library.util.web.Page;
import com.book.renew.renovae.library.util.web.Param;
import com.book.renew.renovae.library.util.web.UrlParser;

import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
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


    public String getDocNumber() {
        return _docNumber;
    }

    public String getItemSequence() {
        return _itemSequence;
    }


    @Override
    public void load(ILibrary library)
            throws NetworkException, UnexpectedPageContentException, LogoutException {
        Page borrow_page = new Page(_borrowLink);
        FmuUtils.checkLogout(borrow_page);
        Elements borrowsTrs = borrow_page.getDoc().select("table:nth-last-of-type(2) > tbody > tr");
        if (borrowsTrs.size() < 3)
            throw new UnexpectedPageContentException();
        _borrowDate = FmuUtils.tryParse(borrowsTrs.eq(0).select("td").eq(1).text());
        System.out.println(borrowsTrs.eq(4).text());
        Matcher matcher = FmuUtils.FIND_RENOVATIONS.matcher(borrowsTrs.eq(4).select("td:last-of-type").text());
        if (!matcher.matches())
            throw new UnexpectedPageContentException();
        int renovations = Integer.parseInt(matcher.group(1));
        _maxRenovations = Integer.parseInt(matcher.group(2));

        Elements renewLink = borrowsTrs.eq(2).select("td:eq(1) a");
        if (renewLink.isEmpty()) {
            //It is empty, can't renew
            String msg = borrowsTrs.eq(2).select("td:eq(1)").text();
            if (msg != null && !msg.equals("")) {
                if (msg.indexOf("Limite de renov") != -1) {
                    _cause = new CantRenewCause(CantRenewCause.Cause.MAX_RENEWS, "Máximo de renovações alcançado: 5/5");
                    renovations = 3;
                }
                else
                    _cause = new CantRenewCause(CantRenewCause.Cause.OVERDUE);
            }
            else
                _cause = new CantRenewCause(CantRenewCause.Cause.UNKNOWN);
            _renovations = renovations;
            _canRenew = false;
        }
        else {
            //Can renew
            _canRenew = true;
            _cause = null;
        }
    }


    @Override
    public void do_renew(ILibrary library)
            throws NetworkException, RenewException, UnexpectedPageContentException, LogoutException {
        FmuLibrary fmuLibrary = (FmuLibrary) library;
        Page renew_page = new Page(fmuLibrary.getBaseUrl(), new ArrayList<Param>(
                Arrays.asList(
                        new Param("func", "bor-renew-all"),
                        new Param("renew_selected", "Y"),
                        new Param("c" + getDocNumber() + getItemSequence(), "Y")
                )
        ));
        FmuUtils.checkLogout(renew_page);

        Elements tdDueDate = renew_page.getDoc().select("table:last-of-type > tbody > tr:last-of-type td:eq(3)");
        if (tdDueDate.size() != 1)
            throw new UnexpectedPageContentException();

        Date newDueDate = FmuUtils.tryParse(tdDueDate.text());
        if (Util.dateDiff(getDueDate(), newDueDate) < 1) {
            Elements tdCause = renew_page.getDoc().select("table:last-of-type > tbody > tr:last-of-type td:eq(8)");
            if (tdCause.size() != 1)
                throw new RenewException(new CantRenewCause(CantRenewCause.Cause.UNKNOWN));
            String causeMessage = tdCause.text();

            if (causeMessage.indexOf("sem modificação da data de devolução") != -1) {
                throw new RenewException(new CantRenewCause(CantRenewCause.Cause.CANT_EXTEND, causeMessage));
            }
            else if (causeMessage.indexOf("Limite de renovação alcançado") != -1) {
                throw new RenewException(new CantRenewCause(CantRenewCause.Cause.CANT_EXTEND, "Máximo de renovações alcançado: 3/3"));
            }
            else if (causeMessage.indexOf("em atraso") != -1) {
                throw new RenewException(new CantRenewCause(CantRenewCause.Cause.OVERDUE, causeMessage));
            }
            throw new RenewException(new CantRenewCause(CantRenewCause.Cause.UNKNOWN, causeMessage));

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
