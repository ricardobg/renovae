package com.book.renew.renovae.library.api;

import com.book.renew.renovae.library.Book;
import com.book.renew.renovae.utils.Util;

import java.io.IOException;
import java.util.Date;

/**
 * Created by ricardo on 27/07/16.
 * Representa um empréstimo, deve ser implementado para cada universidade
 */
public abstract class IBorrow {

    protected Book _book;
    protected Date _borrow_date;
    protected Date _due_date;


    public IBorrow(Book book, Date borrow_date, Date due_date) {
        _book = book;
        _borrow_date = borrow_date;
        _due_date = due_date;
    }

    public abstract void renew() throws IOException, UnexpectedPageContent;

    public Book getBook() {
        return _book;
    }
    public Date getBorrowDate() {
        return _borrow_date;
    }
    public Date getDueDate() {
        return _due_date;
    }

    @Override
    public String toString() {
        return "Empréstimo do livro " + _book.getTitle()
                + " realizado em " + Util.DATE_FORMAT.format(_borrow_date)
                + " com vencimento em " + Util.DATE_FORMAT.format(_due_date);

    }

}
