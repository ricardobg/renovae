package com.book.renew.renovae.view.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.book.renew.renovae.library.LoginParameters;
import com.book.renew.renovae.util.UserPreferences;

/**
 * Start activity of the app
 */
public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Get preferences and send in intent (preferences can be null)
        UserPreferences prefs = new UserPreferences(StartActivity.this);
        LoginParameters login = prefs.getLogin();
        //Saved login: go to borrowed books activity
        if (login != null)
            startActivity(MainActivity.newIntent(StartActivity.this, login));
        else
            startActivity(LoginActivity.newIntent(StartActivity.this));
        finish();

    }
}
