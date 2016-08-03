package com.book.renew.renovae;

import com.book.renew.renovae.library.IBorrow;
import com.book.renew.renovae.library.impl.usp.UspLibrary;

import org.junit.Test;

import java.util.List;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */

public class UspLibraryUnitTest {
    @Test
    public void url() throws Exception {
        UspLibrary l = new UspLibrary();
        l.login("8041992", "0604");
     //   List<IBorrow> borrows = l.getBorrowedBooks();
     //   for (IBorrow b : borrows) {
     //       System.out.println(b);
    //         b.renew();
      //  }
    }


}