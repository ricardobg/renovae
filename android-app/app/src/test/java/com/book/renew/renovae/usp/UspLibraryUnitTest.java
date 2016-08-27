package com.book.renew.renovae.usp;


import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.impl.usp.UspLibrary;
import com.book.renew.renovae.util.ILogger;
import com.book.renew.renovae.util.Util;
import com.book.renew.renovae.library.util.web.Page;

import org.junit.Before;
import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */

public class UspLibraryUnitTest  {
    @Before
    public void changeImpl() {
        Util.setLogger(new ILogger() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }
        });
        Page.setCrawler(new UspTestCrawler());
    }
    @Test
    public void test1() throws Exception {
        UspLibrary lib = new UspLibrary();
        lib.login("123","123");
        for (IBorrow b : lib.loadBorrowsList()) {
            Util.log(b);
            b.load(lib);
            Util.log(b.canRenew());
        }
    }




}