package com.book.renew.renovae;

import android.util.Log;

import com.book.renew.renovae.library.api.IBorrow;
import com.book.renew.renovae.library.api.usp.UspLibrary;
import com.book.renew.renovae.utils.web.Page;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */

public class UspLibraryUnitTest {
    @Test
    public void url() throws Exception {
        /*UspLibrary l = new UspLibrary();
        List<IBorrow> borrows = l.getBorrowedBooks();
        for (IBorrow b : borrows) {
            System.out.println(b);
            b.renew();
        }*/
    }

}