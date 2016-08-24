package com.book.renew.renovae.usp;

import com.book.renew.renovae.ITestCrawler;
import com.book.renew.renovae.util.web.Page;
import com.book.renew.renovae.util.web.Param;

import java.io.File;
import java.util.List;

/**
 * Created by ricardo on 24/08/16.
 */
public class UspTestCrawler extends ITestCrawler {

    private enum State {
        FIRST_ACCESS, NOT_LOGGED, LOGGED
    }

    private State state = State.FIRST_ACCESS;
    @Override
    public File getPageFile(String url, Page.Method method, List<Param> getParams, List<Param> postParams) {
        if (state == State.FIRST_ACCESS) {
            return new File(getClass().getResource("initial_page.html").getFile());
        }
        else if (state == State.NOT_LOGGED) {

        }
        else {

        }
        return null;
    }
}
