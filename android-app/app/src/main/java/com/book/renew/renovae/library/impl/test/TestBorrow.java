package com.book.renew.renovae.library.impl.test;

import com.book.renew.renovae.library.impl.Book;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.exception.renew.CantRenewCause;
import com.book.renew.renovae.library.exception.renew.RenewException;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.impl.ILibrary;

import java.util.Date;
import java.util.Random;

/**
 * Created by ricardo on 26/08/16.
 */
public class TestBorrow extends IBorrow {
    private int id;

    public TestBorrow(int id, Book book, Date due_date) {
        super(book, due_date);
        this.id = id;
        Random r = new Random();
        _canRenew = !(r.nextInt(6) == 0);
        if (!_canRenew) {
            switch (r.nextInt(3)) {
                case 0:
                    _cause = new CantRenewCause(CantRenewCause.Cause.CANT_EXTEND);
                    break;
                case 1:
                    _cause = new CantRenewCause(CantRenewCause.Cause.MAX_RENEWS);
                    break;
                case 2:
                    _cause = new CantRenewCause(CantRenewCause.Cause.OVERDUE);
                    break;
                case 3:
                    _cause = new CantRenewCause(CantRenewCause.Cause.UNKNOWN);
                    break;
            }

        }
    }

    @Override
    public int getMaxRenews() {
        return 3;
    }

    @Override
    protected void do_renew(ILibrary library) throws RenewException, NetworkException, UnexpectedPageContentException, LogoutException {
        Random r = new Random();
        _dueDate =  new Date(_dueDate.getTime() + (r.nextInt(10))*(1000*60*60*24));
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        _canRenew = false;
        _cause = new CantRenewCause(CantRenewCause.Cause.MAX_RENEWS);
    }

    @Override
    public void load(ILibrary library) throws NetworkException, UnexpectedPageContentException, LogoutException {

    }

    @Override
    public boolean equals(Object obj) {
        return this.id == ((TestBorrow) obj).id;
    }

    @Override
    public String toString() {
        return String.valueOf(id);
    }
}
