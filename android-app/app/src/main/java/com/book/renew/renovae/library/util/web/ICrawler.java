package com.book.renew.renovae.library.util.web;

import com.book.renew.renovae.library.exception.network.NetworkException;

import java.util.List;

/**
 * Interface to download Page
 */
public interface ICrawler {
    String download(String url, Page.Method method, List<Param> getParams,
                List<Param> postParams) throws NetworkException;
}
