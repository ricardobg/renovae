package com.book.renew.renovae.android;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by ricardo on 25/08/16.
 */
public class Util {
    public static final String EXTRA_ERROR_MESSAGE = "com.book.renew.renovae.error_message";

    public static final String EXTRA_LOGIN_PARAMETERS = "com.book.renew.renovae.login_parameters";

    public static boolean hasInternetConnection(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }
}
