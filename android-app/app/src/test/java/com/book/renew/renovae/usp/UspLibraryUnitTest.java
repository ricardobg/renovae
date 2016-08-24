package com.book.renew.renovae.usp;

import com.book.renew.renovae.library.impl.usp.UspLibrary;
import com.book.renew.renovae.util.web.Page;

import org.junit.Test;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */

public class UspLibraryUnitTest {
    @Test
    public void url() throws Exception {
        Page.setCrawler(new UspTestCrawler());
        UspLibrary l = new UspLibrary();
    }


}