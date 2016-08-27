package com.book.renew.renovae.usp;

import com.book.renew.renovae.ITestCrawler;
import com.book.renew.renovae.library.util.web.Page;
import com.book.renew.renovae.library.util.web.Param;

import java.io.File;
import java.util.List;

/**
 * Created by ricardo on 24/08/16.
 */
public class UspTestCrawler extends ITestCrawler {

   // public UspTestCrawler

    private enum State {
        FIRST_ACCESS, NOT_LOGGED, LOGGED
    }

    private State state = State.FIRST_ACCESS;
    @Override
    public File getPageFile(String url, Page.Method method, List<Param> getParams, List<Param> postParams) {
        if (state == State.FIRST_ACCESS) {
            state = State.NOT_LOGGED;
            return new File(BASE_DIR + "library_pages/usp/test_0/initial.html");
        }
        else if (state == State.NOT_LOGGED) {
            if (hasParam("func", "login-session", getParams)) {
                if (hasParam("bor_id", "123", getParams)
                        && hasParam("bor_verification", "123", getParams)) {
                    state = State.LOGGED;
                    return new File(BASE_DIR + "library_pages/usp/test_0/after_login.html");
                }
            }
            return null;
        }
        else if (state == State.LOGGED){
            if (hasParam("func", "bor-loan", getParams)) {
                return new File(BASE_DIR + "library_pages/usp/test_0/borrows.html");
            }
            if (hasParam("func", "BOR-LOAN-EXP", getParams)) {
                return new File(BASE_DIR + "library_pages/usp/test_0/borrow_1.html");
            }
        }
        return null;
    }
}
