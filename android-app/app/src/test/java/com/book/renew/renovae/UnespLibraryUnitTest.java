package com.book.renew.renovae;

import com.book.renew.renovae.library.impl.unesp.UnespLibrary;
import com.book.renew.renovae.util.ILogger;
import com.book.renew.renovae.util.Util;


import org.junit.Test;

/**
 * Created by ricardo on 29/07/16.
 */
public class UnespLibraryUnitTest {
    @Test
    public void url() throws Exception {
        Util.setLogger(new ILogger() {
            @Override
            public void log(String message) {
                System.out.println(message);
            }
        });
        UnespLibrary l = new UnespLibrary();
        l.login("","");

      //  List<IBorrow> borrows = l.getBorrowedBooks();
      //  for (IBorrow b : borrows) {
       //     System.out.println(b);
          //  b.renew();
      //  }
    }
}
