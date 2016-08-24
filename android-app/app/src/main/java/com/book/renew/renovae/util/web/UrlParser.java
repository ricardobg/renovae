package com.book.renew.renovae.util.web;

import android.net.UrlQuerySanitizer;

/**
 * Class to parse url parameters and get values
 */
public class UrlParser {
    private UrlQuerySanitizer _sanatizer;

    public UrlParser(String base_url) {
        _sanatizer = new UrlQuerySanitizer();
        _sanatizer.setAllowUnregisteredParamaters(true);
        _sanatizer.parseUrl(base_url);
    }

    /**
     * Returns the string of param
     * @param param
     * @return
     */
    public String get(String param) {
        return _sanatizer.getValue(param);
    }
}
