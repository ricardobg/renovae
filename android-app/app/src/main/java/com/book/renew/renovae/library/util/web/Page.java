package com.book.renew.renovae.library.util.web;

import com.book.renew.renovae.library.exception.network.NetworkException;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.util.List;

/**
 * Created by ricardo on 28/07/16.
 */
public class Page {

    public enum Method {
        POST, GET
    }

    private static ICrawler _crawler = new WebCrawler();

    public Page(String url) throws NetworkException {
        this(url, Method.GET);
    }
    public Page(String url, Method method) throws NetworkException {
        this(url, method, null);
    }
    public Page(String url, Method method, List<Param> getParams) throws NetworkException {
        this(url, method, getParams, null);
    }
    public Page(String url, List<Param> getParams) throws NetworkException {
        this(url, Method.GET, getParams, null);
    }
    public Page(String url, Method method, List<Param> getParams,
                List<Param> postParams) throws NetworkException {
        this.doc = null;
        this.content = _crawler.download(url, method, getParams, postParams);
    }

    public static void setCrawler(ICrawler crawler) {
        _crawler = crawler;
    }



    public String getContent() {
        return content;
    }
    public Document getDoc() {
        if (doc == null)
            doc = Jsoup.parse(content);

        return doc;
    }

    protected static String buildParamsString(List<Param> params) {
        StringBuilder ret = new StringBuilder();
        for (Param entry : params) {
            ret.append(entry.toString());
            ret.append("&");
        }
        ret.deleteCharAt(ret.length() - 1);
        return ret.toString();
    }

    private Document doc = null;
    protected String content;
}
