package com.book.renew.renovae.library.impl.usp;

import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.util.web.Page;

import org.jsoup.Jsoup;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ricardo on 03/08/16.
 * Utility functions for USP library
 */
public class UspUtils {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");

    public static String getFeedback(Page page) {
        Elements feedback =  page.getDoc().select("table.tablebar > tbody > tr > td#feedback_bar.feedbackbar");
        if (!feedback.isEmpty() && !feedback.text().replaceAll("\\s|\\u00a0", "").isEmpty())
            return feedback.text();
        return null;
    }

    public static String getFeedback(String content) {
        Elements feedback =  Jsoup.parse(content).select("table.tablebar > tbody > tr > td#feedback_bar.feedbackbar");
        if (!feedback.isEmpty() && !feedback.text().replaceAll("\\s|\\u00a0", "").isEmpty())
            return feedback.text();
        return null;
    }

    public static Date tryDateParse(String date) throws UnexpectedPageContentException {
        try {
            return DATE_FORMAT.parse(date);
        } catch (ParseException e) {
            throw new UnexpectedPageContentException();
        }
    }

    public static void checkLogout(Page page) throws LogoutException {
        Elements user_button = page.getDoc().select("table.tablebar > tbody > tr.topbar > td.topbar > a:matches(.*Usu.rio.*)");
        if (user_button.size() == 0)
            throw new LogoutException();
    }
}
