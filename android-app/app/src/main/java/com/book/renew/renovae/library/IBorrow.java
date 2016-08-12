package com.book.renew.renovae.library;

import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.RenewException;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;
import com.book.renew.renovae.util.Util;

import java.io.IOException;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by ricardo on 27/07/16.
 * Representa um empréstimo, deve ser implementado para cada universidade
 */
public abstract class IBorrow implements Serializable, Comparable<IBorrow> {

    protected Book _book;
    protected Date _due_date;


    public IBorrow(Book book, Date due_date) {
        _book = book;
        _due_date = due_date;
    }

    public abstract void renew() throws IOException, UnexpectedPageContent, RenewException, LogoutException;

    public Book getBook() {
        return _book;
    }
    public Date getDueDate() {
        return _due_date;
    }

    @Override
    public String toString() {
        return "Empréstimo do livro " + _book.getTitle()
                + " com vencimento em " + Util.FULL_YEAR_DATE_FORMAT.format(_due_date);

    }

    @Override
    public int compareTo(IBorrow cmp) {
        int cmp_due_date = _due_date.compareTo(cmp._due_date);
        if (cmp_due_date != 0)
            return cmp_due_date;

        int cmp_borrow_date = _due_date.compareTo(cmp._due_date);
        if (cmp_borrow_date != 0)
            return cmp_borrow_date;

        return _book.compareTo(cmp._book);
    }

}
