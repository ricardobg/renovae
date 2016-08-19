package com.book.renew.renovae.library.exception.network;

import com.book.renew.renovae.library.exception.DefaultMessageException;

/**
 * Created by ricardo on 18/08/16.
 */
public class NetworkException extends DefaultMessageException {
    public NetworkException(String message) { super(message, "Erro de rede"); }
}
