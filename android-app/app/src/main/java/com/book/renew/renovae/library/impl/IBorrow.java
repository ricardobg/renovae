package com.book.renew.renovae.library.impl;


import com.book.renew.renovae.library.Book;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.exception.renew.CantRenewCause;
import com.book.renew.renovae.library.exception.renew.RenewException;
import com.book.renew.renovae.util.Util;
import java.io.Serializable;
import java.util.Date;

/**
 * Class to represent a borrow
 */
public abstract class IBorrow implements Serializable, Comparable<IBorrow> {

    protected Book _book;
    protected Date _dueDate;
    protected Date _borrowDate;
    protected CantRenewCause _cause;
    protected boolean _canRenew;
    protected int _renovations;

    /**
     * Get the max number of times a title can be renewed
     * @return max number of renovations
     */
    public abstract int getMaxRenews();

    /**
     * Renews a borrow. Updating this instance data
     * @param library the library instance to get session data
     * @throws RenewException
     * @throws NetworkException
     * @throws UnexpectedPageContentException
     * @throws LogoutException
     */
    public final void renew(ILibrary library) throws RenewException, NetworkException,
            UnexpectedPageContentException, LogoutException {
        if (!_canRenew)
            throw new RenewException(_cause);
        //Renew
        do_renew(library);
        //Loads
        load(library);
    }

    /**
     * Function that actually go to the library website and renews the borrow
     * @throws RenewException
     * @throws NetworkException
     * @throws UnexpectedPageContentException
     * @throws LogoutException
     */
    protected abstract void do_renew(ILibrary library)  throws RenewException, NetworkException,
            UnexpectedPageContentException, LogoutException;

    /**
     * Function that loads all data of the borrow.
     * The constructor (and the ILibrary) shouldn't load
     * any data that needs a new http request, just get
     * the data from the list of borrows. Ths function
     * loads all remaing data (if any).
     * By all data, we mean:
     * Book, due date, borrow date, can't renew cause,
     * can renew and renovations.
      * @throws NetworkException
     * @throws UnexpectedPageContentException
     * @throws LogoutException
     */
    public abstract void load(ILibrary library)
            throws NetworkException, UnexpectedPageContentException, LogoutException;

    /**
     * IBororw need to implement equals
     * @param obj
     * @return
     */
    @Override
    public abstract boolean equals(Object obj);


    protected IBorrow(Book book, Date dueDate, Date borrowDate, boolean canRenew,
                      int renovations, CantRenewCause cause) {
        this._book = book;
        this._dueDate = dueDate;
        this._borrowDate = borrowDate;
        this._canRenew = canRenew;
        this._renovations = renovations;
        this._cause = cause;
    }

    protected IBorrow(Book book, Date dueDate, Date borrowDate, CantRenewCause state) {
        this(book, dueDate, borrowDate, false, -1, state);
    }
    protected IBorrow(Book book, Date dueDate, Date borrowDate, int renovations) {
        this(book, dueDate, borrowDate, true, renovations, null);
    }
    protected IBorrow(Book book, Date dueDate, Date borrowDate) {
        this(book, dueDate, borrowDate, true, -1, null);
    }
    protected IBorrow(Book book, Date dueDate, int renovations) {
        this(book, dueDate, null, false, renovations, null);
    }
    protected IBorrow(Book book, Date dueDate) {
        this(book, dueDate, null, false, -1, null);
    }

    public Book getBook() {
        return _book;
    }
    public Date getDueDate() {
        return _dueDate;
    }
    public Date getBorrowDate() {
        return _borrowDate;
    }
    public boolean canRenew() { return _canRenew; }
    public CantRenewCause getCause() { return _cause; }

    /**
     * Gets the number of times the borrow was renewed
     * @return The number of times the borrow was renewed or
     * -1 if can't know
     */
    public int getRenovations() { return _renovations; }

    @Override
    public String toString() {
        return "Empréstimo do livro " + _book.getTitle()
                + " com vencimento em " + Util.FULL_YEAR_DATE_FORMAT.format(_dueDate);
    }

    @Override
    public int compareTo(IBorrow cmp) {
        int cmp_due_date = _dueDate.compareTo(cmp._dueDate);
        if (cmp_due_date != 0)
            return cmp_due_date;

        return _book.compareTo(cmp._book);
    }
}
