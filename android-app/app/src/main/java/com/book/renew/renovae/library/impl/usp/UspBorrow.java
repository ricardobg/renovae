package com.book.renew.renovae.library.impl.usp;


import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.Book;
import com.book.renew.renovae.library.exception.renew.CantRenewCause;
import com.book.renew.renovae.library.exception.renew.RenewException;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.impl.ILibrary;
import com.book.renew.renovae.util.Util;
import com.book.renew.renovae.util.web.Page;
import com.book.renew.renovae.util.web.Param;
import com.book.renew.renovae.util.web.UrlParser;

import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

public class UspBorrow extends IBorrow {

    private final String _docNumber;
    private final String _itemSequence;
    private static final int MAX_RENEWS = 3;
    private static final int RENOVATION_DAYS = 10;

    public UspBorrow(Book book, Date due_date,
                     String borrowLink) {
        super(book, due_date);
        //Get book info
        UrlParser parser = new UrlParser(borrowLink);
        _docNumber = parser.get("doc_number");
        _itemSequence = parser.get("item_sequence");
    }

    @Override
    public void load(ILibrary library)
            throws NetworkException, UnexpectedPageContentException, LogoutException {


        int renovations = 0;
        Page borrow_page = new Page(((UspLibrary) library).getBaseUrl(),  Arrays.asList(
                new Param("func", "bor-loan-exp"),
                new Param("doc_number", getDocNumber()),
                new Param("item_sequence", getItemSequence()),
                new Param("bor_library", "USP50")
        ));

        //Check logout
        UspUtils.checkLogout(borrow_page);

        //Try to find table rows (trs)
        Elements borrowsTrs = borrow_page.getDoc().select("table:nth-last-of-type(2) > tbody > tr");
        if (borrowsTrs.size() < 3)
            throw new UnexpectedPageContentException();

        //Get borrow date
        _borrowDate = UspUtils.tryDateParse(borrowsTrs.eq(0).select("td").eq(1).text());

        //Calculate renovations
        int dateDiff = Util.dateDiff(_borrowDate, _dueDate);

        if (dateDiff <= 11)
            renovations = 0;
        //Can't know in other cases
        renovations = -1;

        //Try to figure out if can renew borrow
        Elements renewLink = borrowsTrs.eq(2).select("td:eq(1) a");
        if (renewLink.isEmpty()) {
            //It is empty, can't renew


            String msg = borrowsTrs.eq(2).select("td:eq(1)").text();
            if (msg != null && !msg.equals("")) {
                if (msg.indexOf("Limite de renov") != -1) {
                    _cause = new CantRenewCause(CantRenewCause.Cause.MAX_RENEWS, "Máximo de renovações alcançado: 3/3");
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
        _renovations = renovations;
    }

    private String getDocNumber() {
        return _docNumber;
    }

    private String getItemSequence() {
        return _itemSequence;
    }

    @Override
    public int getMaxRenews() {
        return MAX_RENEWS;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof UspBorrow))
            return false;
        UspBorrow cmp = (UspBorrow) obj;
        if (cmp == this)
            return true;
        return (cmp._docNumber == this._docNumber &&
                cmp._itemSequence == this._itemSequence);
    }



    @Override
    public void do_renew(ILibrary library)
            throws RenewException, NetworkException, UnexpectedPageContentException, LogoutException {

        UspLibrary uspLibrary = (UspLibrary) library;
        Page renew_page = new Page(uspLibrary.getBaseUrl(), new ArrayList<Param>(
                Arrays.asList(
                        new Param("func", "bor-renew-all"),
                        new Param("renew_selected", "Y"),
                        new Param("c" + getDocNumber() + getItemSequence(), "Y"),
                        new Param("bor_library", "USP50")
                )
        ));
        //Check logout
        UspUtils.checkLogout(renew_page);

        //Gets due date
        Elements tdDueDate = renew_page.getDoc().select("table:last-of-type > tbody > tr:last-of-type td:eq(3)");
        if (tdDueDate.size() != 1)
            throw new UnexpectedPageContentException();
        Date newDueDate = UspUtils.tryDateParse(tdDueDate.text());

        //If it was not renewed: new due date == current due date
        if (Util.dateDiff(getDueDate(), newDueDate) < 1) {
            //Gets the cause
            Elements tdCause = renew_page.getDoc().select("table:last-of-type > tbody > tr:last-of-type td:eq(8)");
            if (tdCause.size() != 1)
                throw new RenewException(new CantRenewCause(CantRenewCause.Cause.UNKNOWN));
            String causeMessage = tdCause.text();
            //
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
}
