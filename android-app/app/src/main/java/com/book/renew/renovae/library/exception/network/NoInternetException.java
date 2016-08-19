package com.book.renew.renovae.library.exception.network;

import com.book.renew.renovae.library.exception.DefaultMessageException;

/**
 * Created by ricardo on 18/08/16.
 */
public class NoInternetException extends NetworkException {
    public NoInternetException() {
        super("Sem conexão com a internet");
    }

}
