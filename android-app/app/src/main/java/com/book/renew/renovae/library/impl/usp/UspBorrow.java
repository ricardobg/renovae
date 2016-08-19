package com.book.renew.renovae.library.impl.usp;

import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.impl.Book;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.RenewException;
import com.book.renew.renovae.library.exception.renew.UnknownRenewException;
import com.book.renew.renovae.util.web.Page;

import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.Date;

/**
 * Created by ricardo on 28/07/16.
 */
public class UspBorrow extends IBorrow {
    private String _borrow_link;
    public UspBorrow(Book book, Date due_date, String borrow_link) {
        super(book, due_date);
        _borrow_link = borrow_link;
    }
    protected static void test() {}

    public void renew() throws NetworkException, UnexpectedPageContentException, RenewException, LogoutException {
        Page borrow_page = new Page(_borrow_link);
        System.out.println(_borrow_link);
        Elements borrows_tr = borrow_page.getDoc().select("table:nth-last-of-type(2) > tbody > tr");
        if (borrows_tr.size() < 3)
          throw new UnexpectedPageContentException();
        //Date borrow_date = UspUtils.tryDateParse(borrows_tr.eq(0).select("td").eq(1).text());
        Elements renew_a = borrows_tr.eq(2).select("td:eq(1) a");
        if (renew_a.isEmpty()) {
            //It is empty, error in renew
            String msg = borrows_tr.eq(2).select("td:eq(1)").text();
            if (msg != null && !msg.equals(""))
                throw new RenewException(msg);
            throw new UnknownRenewException();
        }
        String renew_url = renew_a.attr("href");
        Page renewed = new Page(renew_url);
        System.out.println(renew_url);
        String feedback = UspUtils.getFeedback(renewed);
        if (feedback != null)
            throw new RenewException(feedback);
    }

    @Override
    public boolean equals(Object obj) {
        return compareTo((IBorrow) obj) == 0;
    }
}
