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
     * Function that will be called to get borrows list
     * Should access only a constant number of pages.
     * Additional information of the borrow should be
     * loaded in the load method of the borrow
     * @return
     * @throws NetworkException
     * @throws UnexpectedPageContentException
     * @throws LogoutException
     */
    public abstract ArrayList<IBorrow> loadBorrowsList()
            throws NetworkException, UnexpectedPageContentException, LogoutException;
    /**
     * Logout of the system
     */
    public abstract void logout() throws NetworkException, UnexpectedPageContentException;

}
