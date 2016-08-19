package com.book.renew.renovae.library.impl;


import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Interface para ser implementada para cada universidade
 *
 */
public abstract class ILibrary implements Serializable {

    /**
     * function that does the login
     * @param user
     * @param password
     * @throws IOException
     * @throws UnexpectedPageContentException
     * @throws LoginException
     */
    public abstract void login(String user, String password) throws NetworkException, UnexpectedPageContentException, LoginException;

    /**
     * Get list of borrowed books
     * @return
     */
    public abstract ArrayList<IBorrow> getBorrowedBooks() throws NetworkException, UnexpectedPageContentException, LogoutException;

    /**
     * Logout of the system
     */
    public abstract void logout() throws NetworkException, UnexpectedPageContentException;
}
