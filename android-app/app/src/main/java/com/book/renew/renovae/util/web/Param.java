package com.book.renew.renovae.util.web;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * Created by ricardo on 28/07/16.
 */
public class Param {
    public final String key;
    public final String value;
    public final String encoding;
    public Param(String key, String value) {
        this.key = key;
        this.value = value;
        this.encoding = "UTF-8";
    }
    @Override
    public String toString() {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(URLEncoder.encode(key, encoding))
                    .append('=')
                    .append(URLEncoder.encode(value, encoding));
            return sb.toString();
        } catch (UnsupportedEncodingException e) {
            return "ERROR IN ENCODING";
        }
    }
}
