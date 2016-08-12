package com.book.renew.renovae.library.impl.usp;

import com.book.renew.renovae.library.Book;
import com.book.renew.renovae.library.IBorrow;
import com.book.renew.renovae.library.ILibrary;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;
import com.book.renew.renovae.library.exception.UnknownLoginException;
import com.book.renew.renovae.util.web.Page;
import com.book.renew.renovae.util.web.Param;


import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
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

    private String _login_url;

    public UspLibrary() throws UnexpectedPageContent, IOException {
        //First, download page and get the links
        Page homepage = new Page(HOMEPAGE);
        Elements login_button = homepage.getDoc().select("table:first-of-type > tbody > tr.middlebar > td.middlebar a.blue:matches(.*Sign-in.*");
        Matcher mt = null;
        if (login_button.size() != 1) {
            //Não caiu na página Aleph
            Elements script = homepage.getDoc().select("html > head > script");
            if (script.size() != 1)
                throw new UnexpectedPageContent();
            mt = FIND_LOGIN_PAGE.matcher(script.html());
        }
        else {
            mt = FIND_LOGIN_PAGE_ALT.matcher(login_button.attr("href"));
            System.out.println(login_button.attr("href"));
        }
        if (!mt.matches())
            throw new UnexpectedPageContent();
        _login_url = mt.group(1);
    }

    @Override
    public void login(String user, String password) throws IOException, LoginException, UnexpectedPageContent {

        Page login_page = new Page(_login_url, new ArrayList<Param>(
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
            throw new UnexpectedPageContent();
        //Usuário está logado
        System.out.println(login_page);
    }

    @Override
    public ArrayList<IBorrow> getBorrowedBooks()  throws IOException, UnexpectedPageContent, LogoutException {
        Page borrows_page = new Page(_login_url, new ArrayList<Param>(
                Arrays.asList(
                        new Param("func", "bor-loan"),
                        new Param("adm_library", "USP50")
                )
        ));
        Elements user_button = borrows_page.getDoc().select("table.tablebar > tbody > tr.topbar > td.topbar > a:matches(.*Usu.rio.*)");
        if (user_button.size() == 0)
            throw new LogoutException();
        //Primeiro pega número de empréstimos
        Elements m = borrows_page.getDoc().select("table > tbody > tr:only-of-type > td:only-of-type.td1 > a:matches(DEDALUS.*)");
        if (m.size() != 1)
            throw  new UnexpectedPageContent();
        System.out.println(m.text());
        Matcher mt = FIND_NUMBER_OF_LOANS.matcher(m.text());
        if (!mt.matches())
            throw  new UnexpectedPageContent();
        int n_borrows = Integer.parseInt(mt.group(1));
        if (n_borrows == 0)
            return new ArrayList<>();
        //Agora pega trs da tabela para pegar os empréstimos
        Elements trs = borrows_page.getDoc().select("table:last-of-type > tbody > tr:gt(0)");
        if (trs.size() != n_borrows)
            throw  new UnexpectedPageContent();

        ArrayList<IBorrow> borrows = new ArrayList<>(n_borrows);
        for (Element tr : trs) {
            Elements tds = tr.select("td");
            if (tds.size() < 10)
                throw new UnexpectedPageContent();
            Book book = new Book(tds.eq(3).text(), tds.eq(2).text());
            Date due_date = UspUtils.tryDateParse(tds.eq(5).text());
            String borrow_url = tds.eq(0).select("a").attr("href");
            /*Page borrow_page = new Page(borrow_url);
            Elements borrows_tr = borrow_page.getDoc().select("table:nth-last-of-type(2) > tbody > tr");
            if (borrows_tr.size() < 3)
                throw new UnexpectedPageContent();
            Date borrow_date = UspUtils.tryDateParse(borrows_tr.eq(0).select("td").eq(1).text());
            String renew_url = borrows_tr.eq(2).select("td:eq(1) a").attr("href");*/
            borrows.add(new UspBorrow(book, due_date, borrow_url));
        }
        return borrows;
    }

    @Override
    public void logout()  throws IOError, UnexpectedPageContent {
        //TODO: fazer logout
    }


}
