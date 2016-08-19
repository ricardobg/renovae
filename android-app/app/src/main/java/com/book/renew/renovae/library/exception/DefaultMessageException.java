package com.book.renew.renovae.library.exception;

/**
 * Exception with a Default message (in case of null or empty message)
 */
public class DefaultMessageException extends Exception {
    /**
     * Call this when the message can be empty or null
     * @param message
     * @param default_message
     */
    protected DefaultMessageException(String message, String default_message) {
        super((message == null || message.replaceAll("\\s+","").equals("")) ? default_message : message);
    }

    /**
     * Call this when you are sure that message is not null nor empty
     * @param message
     */
    protected DefaultMessageException(String message) {
        super(message);
    }
}
