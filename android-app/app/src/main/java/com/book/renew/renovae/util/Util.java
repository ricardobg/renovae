package com.book.renew.renovae.util;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by ricardo on 28/07/16.
 */
public class Util {

    public static final SimpleDateFormat FULL_YEAR_DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy");

    public static boolean isEmpty(String str) {
        return (str == null || str.replaceAll("\\s+","").equals(""));
    }

    public static int dateDiff(Date start, Date end) {
        return (int) ((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24));
    }

    private static ILogger _logger = new ILogger() {
        @Override
        public void log(String message) {
            Log.v("renovae", message);
        }
    };

    public static void setLogger(ILogger logger) {
        _logger = logger;
    }

    public static <T> void log(T message) {
        _logger.log(message.toString());
    }

    public static int max(int... values) {
        int max = Integer.MIN_VALUE;
        for (int v : values)
            if (v > max)
                max = v;
        return max;
    }
    public static int min(int... values) {
        int min = Integer.MAX_VALUE;
        for (int v : values)
            if (v < min)
                min = v;
        return min;
    }
}
