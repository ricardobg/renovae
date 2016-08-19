package com.book.renew.renovae.library.exception;

/**
 * Created by ricardo on 28/07/16.
 */
public class UnexpectedPageContentException extends DefaultMessageException {
    public UnexpectedPageContentException() {
        super("O site biblioteca fez algo não previsto :O");
    }
}
