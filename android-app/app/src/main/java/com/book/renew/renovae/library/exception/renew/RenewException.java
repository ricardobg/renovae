package com.book.renew.renovae.library.exception.renew;

import com.book.renew.renovae.library.exception.DefaultMessageException;

/**
 * Class to represent a Renew Exception
 * Thrown when couldn't renew
 */
public class RenewException extends DefaultMessageException {
    protected CantRenewCause _cause;

    public RenewException(CantRenewCause cause) {
        super(cause.getMessage());
        this._cause = cause;
    }

    public RenewException() {
        this(new CantRenewCause(CantRenewCause.Cause.UNKNOWN));
    }
    public RenewException(String message) {
        this(new CantRenewCause(CantRenewCause.Cause.UNKNOWN, message));
    }
}
