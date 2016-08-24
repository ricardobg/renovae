package com.book.renew.renovae.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.util.web.ICrawler;
import com.book.renew.renovae.util.web.Page;
import com.book.renew.renovae.util.web.Param;
import com.book.renew.renovae.util.web.WebCrawler;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by ricardo on 28/07/16.
 */
public class Util {


    public static final String EXTRA_ERROR_MESSAGE = "com.book.renew.renovae.error_message";

    public static final String EXTRA_LOGIN_PARAMETERS = "com.book.renew.renovae.login_parameters";


    public static final SimpleDateFormat FULL_YEAR_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    public static boolean isEmpty(String str) {
        return (str == null || str.replaceAll("\\s+","").equals(""));
    }

    public static int dateDiff(Date start, Date end) {
        return (int) ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));

    }
}
