package com.book.renew.renovae.library.api;


import java.io.IOError;
import java.io.IOException;
import java.util.List;

/**
 * Interface para ser implementada para cada universidade
 *
 */
public interface ILibrary {
    /**
     * Get list of borrowed books
     * @return
     */
    List<IBorrow> getBorrowedBooks() throws IOException, UnexpectedPageContent;

    /**
     * login in the system. Throws an error if it was not possible
     * @param user
     * @param password
     */
    void login(String user, String password) throws IOException, UnexpectedPageContent, LoginException;

    /**
     * Logout of the system
     */
    void logout() throws IOException, UnexpectedPageContent;
}
