package com.book.renew.renovae.library.impl.fmu;

import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.impl.Book;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.impl.ILibrary;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.LogoutException;
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
 * Created by ricardo on 29/07/16.
 */
public class FmuLibrary extends ILibrary {
    public static final String HOMEPAGE = "http://200.229.239.12/F/";
    public static final Pattern FIND_NUMBER_OF_LOANS = Pattern.compile("^\\s*Administrativa\\s*\\-\\s*([0-9]+)\\s*$", Pattern.MULTILINE | Pattern.DOTALL);

    private String _baseUrl;
    private String _tokenId;


    public String getBaseUrl() {
        return _baseUrl;
    }
    public FmuLibrary() throws UnexpectedPageContentException, NetworkException {
        Page homepage = new Page(HOMEPAGE);

        Elements login_button = homepage.getDoc().select("table > tbody > tr.middlebar > td.middlebar a:matches(\\s*Login\\s*)");
        if (login_button.size() != 1)
            throw new UnexpectedPageContentException();

        _baseUrl = login_button.attr("href");
        _baseUrl = _baseUrl.substring(0, _baseUrl.indexOf("?"));
        System.out.println(_baseUrl);
    }

    @Override
    public void login(String user, String password) throws NetworkException, UnexpectedPageContentException, LoginException {
        Page login_page = new Page(_baseUrl, Page.Method.POST, null, new ArrayList<Param>(
                Arrays.asList(
                        new Param("func", "login-session"),
                        new Param("bor_id", user),
                        new Param("bor_verification", password),
                        new Param("bor_library", "FMU50")
                )
        ));
        //Tenta achar botão para ir ao usuário
        Elements login_button = login_page.getDoc().select("table > tbody > tr.middlebar > td.middlebar a:matches(\\s*Login\\s*)");
        System.out.println(login_button);
        if (!login_button.isEmpty()) {
            String feedback = FmuUtils.getFeedback(login_page);
            if (feedback != null)
                throw new LoginException(feedback);
            else
                throw new LoginException("Login falhou");
        }
        Elements user_page = login_page.getDoc().select("table > tbody > tr.middlebar > td.middlebar > a:matches(\\s*Usu.rio\\s*)");
        if (user_page.size() > 1)
            throw new UnexpectedPageContentException();
        //Usuário está logado
        System.out.println("Login ok!");
    }

    @Override
    public ArrayList<IBorrow> loadBorrowsList()
            throws NetworkException, UnexpectedPageContentException, LogoutException {
        Page borrows_page = new Page(_baseUrl, new ArrayList<Param>(
                Arrays.asList(
                        new Param("func", "bor-loan"),
                        new Param("adm_library", "FMU50")
                )
        ));
        FmuUtils.checkLogout(borrows_page);
        //Primeiro pega número de empréstimos
        Elements m = borrows_page.getDoc().select("table > tbody > tr:only-of-type > td:only-of-type.td1 > a:matches(Administrativa.*)");
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
            Book book = new Book(tds.eq(3).text().substring(0, tds.eq(3).text().indexOf('/') == -1 ? tds.eq(3).text().length() :
                    tds.eq(3).text().indexOf('/')), tds.eq(2).text());
            Date due_date = FmuUtils.tryParse(tds.eq(5).text());
            String borrow_url = tds.eq(0).select("a").attr("href");
            borrows.add(new FmuBorrow(book, due_date, borrow_url));

        }
        return borrows;
    }

    @Override
    public void logout() throws NetworkException, UnexpectedPageContentException {

    }


}
