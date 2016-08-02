package com.book.renew.renovae.library.exception.renew;

import com.book.renew.renovae.library.exception.RenewException;

/**
 * Created by ricardo on 02/08/16.
 */
public class OverdueRenewException extends RenewException {
    public OverdueRenewException(String message) {
        super(message);
    }
}
