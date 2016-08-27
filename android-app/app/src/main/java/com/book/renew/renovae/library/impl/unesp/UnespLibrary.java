package com.book.renew.renovae.library.impl.unesp;

import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.impl.Book;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.impl.ILibrary;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.UnknownLoginException;
import com.book.renew.renovae.util.Util;
import com.book.renew.renovae.library.util.web.Page;
import com.book.renew.renovae.library.util.web.Param;


import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by ricardo on 27/07/16.
 */
public class UnespLibrary extends ILibrary {

    public static final String HOMEPAGE = "http://www.athena.biblioteca.unesp.br/F";
    public static final Pattern FIND_LOGIN_PAGE_ALT = Pattern.compile("([^\\?]+)\\?.*", Pattern.MULTILINE | Pattern.DOTALL);
    public static final Pattern FIND_NUMBER_OF_LOANS = Pattern.compile("^\\s*DEDALUS\\s*\\-\\s*([0-9]+)\\s*$", Pattern.MULTILINE | Pattern.DOTALL);

    private String _baseUrl;

    String getBaseUrl() {
        return _baseUrl;
    }

    public UnespLibrary() throws UnexpectedPageContentException, NetworkException {
        //First, download page and get the links
        Page homepage = new Page(HOMEPAGE);
        Elements loginButton = homepage.getDoc().select("table:first-of-type > tbody > tr.middlebar > td.middlebar a.blue:matches(\\s*Identificação\\s*)");
        String rawUrl = loginButton.attr("href");
        _baseUrl = rawUrl.substring(0, rawUrl.indexOf('?'));

        Util.log("ATHENA URL: " + _baseUrl);
    }

    @Override
    public void login(String user, String password) throws NetworkException, LoginException, UnexpectedPageContentException {

        Page loginPage = new Page(_baseUrl, Page.Method.POST, null, new ArrayList<>(
                Arrays.asList(
                        new Param("ssl_flag", "Y"),
                        new Param("func", "login-session"),
                        new Param("login_source", "LOGIN-BOR"),
                        new Param("bor_id", user),
                        new Param("bor_verification", password),
                        new Param("bor_library", "UEP50")
                )
        ));
        //Tenta achar botão para ir ao usuário
        Elements loginButton = loginPage.getDoc().select("table:first-of-type > tbody > tr.middlebar > td.middlebar > a:matches(.*Identificação.*)");
        if (!loginButton.isEmpty()) {
            String feedback = UnespUtils.getFeedback(loginPage);
            if (feedback != null)
                throw new LoginException(feedback);
            else
                throw new UnknownLoginException();
        }
        //Usuário está logado
        Util.log("Logged in");
    }

    @Override
    public ArrayList<IBorrow> loadBorrowsList()
            throws NetworkException, UnexpectedPageContentException, LogoutException {
     /*   Page borrows_page = new Page(_baseUrl, new ArrayList<Param>(
                Arrays.asList(
                        new Param("func", "bor-loan"),
                        new Param("adm_library", "USP50")
                )
        ));
        UspUtils.checkLogout(borrows_page);
        //Primeiro pega número de empréstimos
        Elements m = borrows_page.getDoc().select("table > tbody > tr:only-of-type > td:only-of-type.td1 > a:matches(DEDALUS.*)");
        if (m.size() != 1)
            throw  new UnexpectedPageContentException();
        Matcher mt = FIND_NUMBER_OF_LOANS.matcher(m.text());
        if (!mt.matches())
            throw  new UnexpectedPageContentException();
        int n_borrows = Integer.parseInt(mt.group(1));
        if (n_borrows == 0)
            return new ArrayList<>();
        //Agora pega trs da tabela para pegar os empréstimos
        Elements trs = borrows_page.getDoc().select("table:last-of-type > tbody > tr:gt(0)");
        if (trs.size() != n_borrows)
            throw  new UnexpectedPageContentException();

        ArrayList<IBorrow> borrows = new ArrayList<>(n_borrows);

        for (Element tr : trs) {
            Elements tds = tr.select("td");
            if (tds.size() < 10)
                throw new UnexpectedPageContentException();
            Book book = new Book(tds.eq(3).text(), tds.eq(2).text());
            Date due_date = UspUtils.tryDateParse(tds.eq(5).text());
            String borrow_url = tds.eq(0).select("a").attr("href");
            borrows.add(new UspBorrow(book, due_date, borrow_url));
        }
        return borrows;*/
        return new ArrayList<>();
    }

    @Override
    public void logout() throws NetworkException, UnexpectedPageContentException {
        //TODO: fazer logout

    }
}
