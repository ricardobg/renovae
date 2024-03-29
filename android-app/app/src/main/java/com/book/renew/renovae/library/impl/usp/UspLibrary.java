package com.book.renew.renovae.library.impl.usp;

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
public class UspLibrary extends ILibrary {

    public static final String HOMEPAGE = "http://dedalus.usp.br/F";
    public static final Pattern FIND_LOGIN_PAGE = Pattern.compile(".*\\&url=([^\']+)\\?.*", Pattern.MULTILINE | Pattern.DOTALL);
    public static final Pattern FIND_LOGIN_PAGE_ALT = Pattern.compile("([^\\?]+)\\?.*", Pattern.MULTILINE | Pattern.DOTALL);
    public static final Pattern FIND_NUMBER_OF_LOANS = Pattern.compile("^\\s*DEDALUS\\s*\\-\\s*([0-9]+)\\s*$", Pattern.MULTILINE | Pattern.DOTALL);

    private String _baseUrl;

    String getBaseUrl() {
        return _baseUrl;
    }

    public UspLibrary() throws UnexpectedPageContentException, NetworkException {
        //First, download page and get the links
        Page homepage = new Page(HOMEPAGE);
        Elements login_button = homepage.getDoc().select("table:first-of-type > tbody > tr.middlebar > td.middlebar a.blue:matches(.*Sign-in.*");
        Matcher mt = null;
        if (login_button.size() != 1) {
            //Não caiu na página Aleph
            Elements script = homepage.getDoc().select("html > head > script");
            if (script.size() != 1)
                throw new UnexpectedPageContentException();
            mt = FIND_LOGIN_PAGE.matcher(script.html());
        }
        else {
            mt = FIND_LOGIN_PAGE_ALT.matcher(login_button.attr("href"));
        }
        if (!mt.matches())
            throw new UnexpectedPageContentException();
        _baseUrl = mt.group(1);
        Util.log("Dedalus URL: " + _baseUrl);
    }

    @Override
    public void login(String user, String password) throws NetworkException, LoginException, UnexpectedPageContentException {

        Page login_page = new Page(_baseUrl, new ArrayList<Param>(
                Arrays.asList(
                        new Param("func", "login-session"),
                        new Param("bor_id", user),
                        new Param("bor_verification", password),
                        new Param("bor_library", "USP50")
                )
        ));
        //Tenta achar botão para ir ao usuário
        Elements user_page = login_page.getDoc().select("table.tablebar > tbody > tr.topbar > td.topbar > a:matches(.*Usu.rio.*)");
        if (user_page.isEmpty()) {
            String feedback = UspUtils.getFeedback(login_page);
            if (feedback != null)
                throw new LoginException(feedback);
            else
                throw new UnknownLoginException();
        }
        if (user_page.size() > 1)
            throw new UnexpectedPageContentException();
        //Usuário está logado
        Util.log("Logged in");
    }

    @Override
    public ArrayList<IBorrow> loadBorrowsList()
            throws NetworkException, UnexpectedPageContentException, LogoutException {
        Page borrows_page = new Page(_baseUrl, new ArrayList<Param>(
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
        return borrows;
    }

    @Override
    public void logout() throws NetworkException, UnexpectedPageContentException {
        //TODO: fazer logout

    }
}
