package com.book.renew.renovae.library.exception;

/**
 * Created by ricardo on 28/07/16.
 */
public class LoginException extends DefaultMessageException {
    public LoginException(String message) {
        super(message, "Não foi possível fazer login");
    }
    protected LoginException() {
        this(null);
    }
}
