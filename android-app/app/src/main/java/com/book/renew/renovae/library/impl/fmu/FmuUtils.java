package com.book.renew.renovae.library.impl.fmu;

import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.util.web.Page;

import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Pattern;

/**
 * Created by ricardo on 20/08/16.
 */
public class FmuUtils {

    public static final SimpleDateFormat DATETIME_FORMAT = new SimpleDateFormat("dd/MM/yy HH:mm");
    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
    public static final Pattern FIND_RENOVATIONS = Pattern.compile("^\\s*Renova..o\\s+([0-9]+)\\s+de\\s+([0-9]+)\\s*$", Pattern.MULTILINE | Pattern.DOTALL);

    public static String getFeedback(Page page) {
        Elements feedback =  page.getDoc().select("table > tbody > tr > td.feedbackbar");
        if (!feedback.isEmpty() && !feedback.text().isEmpty())
            return feedback.text();
        return null;
    }

    public static Date tryParse(String date) throws UnexpectedPageContentException {
        try {
            return DATETIME_FORMAT.parse(date);
        } catch (ParseException e) {
            try {
                return DATE_FORMAT.parse(date);
            }
            catch (ParseException e2) {
                throw new UnexpectedPageContentException();
            }
        }
    }

    public static void checkLogout(Page page)
            throws LogoutException {
        Elements login_button = page.getDoc().select("table > tbody > tr.middlebar > td.middlebar a:matches(\\s*Login\\s*)");
        if (!login_button.isEmpty())
            throw new LogoutException();
    }
}
