package com.book.renew.renovae;

import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.util.web.ICrawler;
import com.book.renew.renovae.util.web.Page;
import com.book.renew.renovae.util.web.Param;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

/**
 *
 * Created by ricardo on 24/08/16.
 */
public abstract class ITestCrawler implements ICrawler {
    public String download(String url, Page.Method method, List<Param> getParams,
                    List<Param> postParams) throws NetworkException {

        try {
            File file = getPageFile(url, method, getParams, postParams);
            StringBuilder res = new StringBuilder();
            BufferedReader reader = new BufferedReader(new FileReader(file));
            res.append(reader.readLine());
            System.out.println(res.toString());
            return res.toString();
        } catch (FileNotFoundException e) {
            throw new NetworkException("Page not found");
        } catch (IOException e) {
            throw new NetworkException("I/O Error");
        }
    }

    public abstract File getPageFile(String url, Page.Method method, List<Param> getParams,
                                     List<Param> postParams);
}
