package com.book.renew.renovae.util;

import android.content.Context;
import android.content.SharedPreferences;


/**
 * Created by ricardo on 04/08/16.
 * Class to Manage User preferences
 */
public class UserPreferences {

    private final Context _context;

    public UserPreferences(Context context) {
        this._context = context;
    }

    private static final String PREFS_USER = "UserPreferences";

    private static final String PREFS_USER_NAME = "username";
    private static final String PREFS_USER_PASSWORD = "password";
    private static final String PREFS_USER_UNIVERSITY = "university";

    public LoginParameters getLogin() {
        SharedPreferences prefs = _context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE);
        String university = prefs.getString(PREFS_USER_UNIVERSITY, null);
        String username = prefs.getString(PREFS_USER_NAME, null);
        String password = prefs.getString(PREFS_USER_PASSWORD, null);

        if (username == null || university == null || password == null)
            return null;
        return new LoginParameters(username, password, university);
    }

    public void updateLogin(LoginParameters login) {
        SharedPreferences.Editor editor = _context.getSharedPreferences(PREFS_USER, Context.MODE_PRIVATE).edit();
        editor.putString(PREFS_USER_NAME, login.username);
        editor.putString(PREFS_USER_PASSWORD, login.password);
        editor.putString(PREFS_USER_UNIVERSITY, login.university);
        editor.apply();
    }

}
