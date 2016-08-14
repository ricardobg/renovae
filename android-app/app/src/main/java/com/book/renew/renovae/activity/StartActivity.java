package com.book.renew.renovae.activity;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.book.renew.renovae.util.LoginParameters;
import com.book.renew.renovae.util.UserPreferences;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Do not make visible
        //setContentView(R.layout.activity_start);

        //Get preferences and send in intent (preferences can be null)
        UserPreferences prefs = new UserPreferences(StartActivity.this);
        LoginParameters login = prefs.getLogin();
        if (login != null)
            startActivity(BorrowedBooksActivity.newIntent(StartActivity.this, null));
        else
            startActivity(LoginActivity.newIntent(StartActivity.this, null));
        finish();

    }
}
