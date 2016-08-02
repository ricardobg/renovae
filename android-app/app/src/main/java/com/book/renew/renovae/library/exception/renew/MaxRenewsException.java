package com.book.renew.renovae.library.exception.renew;

import com.book.renew.renovae.library.exception.RenewException;

/**
 * Created by ricardo on 02/08/16.
 */
public class MaxRenewsException extends RenewException {
    public MaxRenewsException(String message) {
        super(message);
    }
}
