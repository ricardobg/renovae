package com.book.renew.renovae.util.web;

import com.book.renew.renovae.library.exception.network.NetworkException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by ricardo on 24/08/16.
 */
public class WebCrawler implements ICrawler {

    @Override
    public String download(String url, Page.Method method, List<Param> get_params,
                              List<Param> post_params) throws NetworkException {
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
                new_url.append(Page.buildParamsString(get_params));
                url = new_url.toString();
            }
            //Creates URL
            URL url_obj = new URL(url);
            conn = (HttpURLConnection) url_obj.openConnection();
            //Check post parameters
            if (method == Page.Method.POST && post_params != null && post_params.size() > 0) {
                conn.setDoOutput(true);
                conn.setRequestMethod("POST");
                //Read post parameters
                byte[] post_data = Page.buildParamsString(post_params).getBytes("UTF-8");
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
            e.printStackTrace();
            throw new NetworkException(e.getMessage());
        }
        return ret.toString();
    }
}
