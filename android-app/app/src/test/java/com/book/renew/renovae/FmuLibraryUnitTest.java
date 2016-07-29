package com.book.renew.renovae;

import com.book.renew.renovae.library.api.IBorrow;
import com.book.renew.renovae.library.api.fmu.FmuLibrary;


import org.junit.Test;

import java.util.List;

/**
 * Created by ricardo on 29/07/16.
 */
public class FmuLibraryUnitTest {
    @Test
    public void url() throws Exception {
        FmuLibrary l = new FmuLibrary();
        /*
        List<IBorrow> borrows = l.getBorrowedBooks();
        for (IBorrow b : borrows) {
            System.out.println(b);
          //  b.renew();
        }*/
    }
}
