package com.book.renew.renovae.library.exception;

/**
 * Created by ricardo on 02/08/16.
 */
public class RenewException extends DefaultMessageException {
    public RenewException(String message) {
        super(message, "Não foi possível renovar");
    }

    protected RenewException(String message, String default_message) {
        super(message, default_message);
    }

    protected RenewException() {
        this(null);
    }
}
