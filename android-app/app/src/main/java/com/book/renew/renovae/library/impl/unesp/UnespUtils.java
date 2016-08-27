package com.book.renew.renovae.library.impl.unesp;

import com.book.renew.renovae.library.util.web.Page;

import org.jsoup.select.Elements;

/**
 * Created by ricardo on 27/08/16.
 */
public class UnespUtils {
    public static String getFeedback(Page page) {
        Elements feedback =  page.getDoc().select("table > tbody > tr > td.feedbackbar");
        if (!feedback.isEmpty() && !feedback.text().replaceAll("\\s|\\u00a0", "").isEmpty())
            return feedback.text();
        return null;
    }
}
