package com.book.renew.renovae.utils.web;

import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

/**
 * Created by ricardo on 28/07/16.
 */
public class Page {
    public enum Method {
        POST, GET
    }

    public Page(String url) throws IOException {
        this(url, Method.GET);
    }
    public Page(String url, Method method) throws IOException {
        this(url, method, null);
    }
    public Page(String url, List<Param> get_params) throws IOException {
        this(url, Method.GET, get_params, null);
    }
    public Page(String url,  Method method, List<Param> get_params) throws IOException {
        this(url, method, get_params, null);
    }

    public Page(String url, Method method, List<Param> get_params, List<Param> post_params) throws IOException {
        //TODO: Network sign on
        HttpURLConnection conn = null;
        StringBuffer ret = new StringBuffer();
        try {
            //Read GET parameters
            if (get_params != null && get_params.size() > 0) {
                StringBuilder new_url = new StringBuilder(url);
                if (url.indexOf('?') == -1)
                    new_url.append('?');
                else
                    new_url.append('&');
                new_url.append(buildParamsString(get_params));
                url = new_url.toString();
            }
            //Creates URL
            URL url_obj = new URL(url);
            conn = (HttpURLConnection) url_obj.openConnection();
            //Check post parameters
            if (method == Method.POST && post_params != null && post_params.size() > 0) {
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                //Read post parameters
                byte[] post_data = buildParamsString(post_params).getBytes("UTF-8");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Length", String.valueOf(post_data.length));
                conn.getOutputStream().write(post_data);

            }
            else
                conn.setRequestMethod("GET");

            InputStreamReader input = new InputStreamReader(conn.getInputStream());
            BufferedReader reader = new BufferedReader(input);
            String line = "";
            do {
                line = reader.readLine();
                ret.append(line);
            }
            while (line != null);
        }
        catch (IOException e) {
            throw e;
        }

        this.content = ret.toString();
    }

    public String getContent() {
        return content;
    }
    public Document getDoc() {
        if (doc == null)
            doc = Jsoup.parse(content);

        return doc;
    }

    private static String buildParamsString(List<Param> params) {
        StringBuilder ret = new StringBuilder();
        for (Param entry : params) {
            ret.append(entry.toString());
            ret.append("&");
        }
        ret.deleteCharAt(ret.length() - 1);
        return ret.toString();
    }

    private Document doc = null;
    private String content;
}
