package com.book.renew.renovae.library.util.web;

import java.util.HashMap;
import java.util.Map;

/**
 * Class to parse url parameters and get values
 */
public class UrlParser {
    private Map<String, String> _paramsMap = new HashMap<>();

    public UrlParser(String base_url) {
        int separationIndex = base_url.indexOf('?');
        if (separationIndex == -1 || separationIndex == base_url.length() - 1)
            return;
        String query = base_url.substring(separationIndex + 1);
        String[] params = query.split("&");
        for (String param : params) {
            String[] split = param.split("=");
            if (split.length != 2)
                continue;
            _paramsMap.put(split[0], split[1]);
        }
    }

    /**
     * Returns the string of param
     * @param param
     * @return
     */
    public String get(String param) {
        return _paramsMap.get(param);
    }
}
