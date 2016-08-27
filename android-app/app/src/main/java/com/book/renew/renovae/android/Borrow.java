package com.book.renew.renovae.android;

import com.book.renew.renovae.library.impl.Book;
import com.book.renew.renovae.library.exception.renew.CantRenewCause;
import com.book.renew.renovae.library.impl.IBorrow;

import java.io.Serializable;
import java.util.Date;

/**
 * Class to represent a borrow.
 * Contains an IBorrow and deal with database
 */
public final class Borrow implements Serializable, Comparable<Borrow>  {
    private IBorrow _borrow;

    Borrow(IBorrow borrow) {
        this._borrow = borrow;
    }

    IBorrow getBorrow() {
        return _borrow;
    }

    public Book getBook() {
        return _borrow.getBook();
    }
    public Date getDueDate() {
        return _borrow.getDueDate();
    }
    public Date getBorrowDate() {
        return _borrow.getBorrowDate();
    }
    public boolean canRenew() { return _borrow.canRenew(); }
    public CantRenewCause getCause() { return _borrow.getCause(); }

    @Override
    public int compareTo(Borrow cmp) {
        int cmp_due_date = getDueDate().compareTo(cmp.getDueDate());
        if (cmp_due_date != 0)
            return cmp_due_date;

        return getBook().compareTo(cmp.getBook());
    }

    @Override
    public boolean equals(Object obj) {
        return _borrow.equals(((Borrow) obj)._borrow);
    }

    @Override
    public String toString() {
        return _borrow.toString();
    }
}
