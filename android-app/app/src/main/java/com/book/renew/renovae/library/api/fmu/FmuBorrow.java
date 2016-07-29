package com.book.renew.renovae.library.api.fmu;

import com.book.renew.renovae.library.Book;
import com.book.renew.renovae.library.api.IBorrow;
import com.book.renew.renovae.library.api.UnexpectedPageContent;

import java.io.IOException;
import java.util.Date;

/**
 * Created by ricardo on 29/07/16.
 */
public class FmuBorrow extends IBorrow {

    private String _renew_link;
    public FmuBorrow(Book book, Date borrow_date, Date due_date, String renew_link) {
        super(book, borrow_date, due_date);
        _renew_link = renew_link;
    }
    @Override
    public void renew() throws IOException, UnexpectedPageContent {
        //TODO: renovar
    }
}
