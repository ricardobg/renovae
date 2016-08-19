package com.book.renew.renovae.library.exception.network;

import com.book.renew.renovae.library.exception.DefaultMessageException;

/**
 * Created by ricardo on 18/08/16.
 */
public class LibraryUnavailableException extends NetworkException {
    public LibraryUnavailableException() { super("Site da biblioteca indisponível"); }
}
