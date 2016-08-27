package com.book.renew.renovae.library.impl.test;

import com.book.renew.renovae.library.impl.Book;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.impl.ILibrary;

import java.util.ArrayList;
import java.util.Date;
import java.util.Random;

/**
 * Created by ricardo on 26/08/16.
 */
public class TestLibrary extends ILibrary {
    @Override
    public void login(String user, String password) throws NetworkException, UnexpectedPageContentException, LoginException {
        if (user.equals("123") && password.equals("123"))
            return;
        throw new LoginException("Usuário/senha inválidos");

    }

    @Override
    public ArrayList<IBorrow> loadBorrowsList() throws NetworkException, UnexpectedPageContentException, LogoutException {
        Random random = new Random();
        int nBorrows = random.nextInt(7) + 3;
        ArrayList<IBorrow> borrows = new ArrayList<>(nBorrows);

        for (int i = 0; i < nBorrows; i++) {
            borrows.add(new TestBorrow(i, new Book(i + " - Título do livro de teste", "Nome do autor de teste"),
                    new Date(System.currentTimeMillis() + (-5 + random.nextInt(10))*(1000*60*60*24))));
        }
        return borrows;
    }

    @Override
    public void logout() throws NetworkException, UnexpectedPageContentException {

    }
}
