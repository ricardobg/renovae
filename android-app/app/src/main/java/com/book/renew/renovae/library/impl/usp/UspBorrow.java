package com.book.renew.renovae.library.impl.usp;

import com.book.renew.renovae.library.Book;
import com.book.renew.renovae.library.IBorrow;
import com.book.renew.renovae.library.exception.RenewException;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;
import com.book.renew.renovae.library.exception.renew.UnknownRenewException;
import com.book.renew.renovae.util.web.Page;

import java.io.IOException;
import java.util.Date;

/**
 * Created by ricardo on 28/07/16.
 */
public class UspBorrow extends IBorrow {
    private String _renew_link;
    public UspBorrow(Book book, Date due_date, String renew_link) {
        super(book, due_date);
        _renew_link = renew_link;
    }

    public void renew() throws IOException, UnexpectedPageContent, RenewException {
        Page renewed = new Page(_renew_link);
        String feedback = UspUtils.getFeedback(renewed);
        if (feedback != null)
            throw new RenewException(feedback);
        else
            throw new UnknownRenewException();
    }
}
