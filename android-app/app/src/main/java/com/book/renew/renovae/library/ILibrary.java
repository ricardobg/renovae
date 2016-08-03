package com.book.renew.renovae.library;


import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

/**
 * Interface para ser implementada para cada universidade
 *
 */
public abstract class ILibrary implements Serializable {
    /**
     * Get list of borrowed books
     * @return
     */
    public abstract List<IBorrow> getBorrowedBooks() throws IOException, UnexpectedPageContent;

    /**
     * login in the system. Throws an error if it was not possible
     * @param user
     * @param password
     */
    public abstract void login(String user, String password) throws IOException, UnexpectedPageContent, LoginException;

    /**
     * Logout of the system
     */
    public abstract void logout() throws IOException, UnexpectedPageContent;
}
