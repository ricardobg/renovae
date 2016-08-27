package com.book.renew.renovae.view.activity;

import android.content.Context;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.book.renew.renovae.R;
import com.book.renew.renovae.android.LibraryManager;
import com.book.renew.renovae.android.LoginParameters;
import com.book.renew.renovae.android.UserPreferences;
import com.book.renew.renovae.android.Util;
import com.book.renew.renovae.view.fragment.BorrowedBooksFragment;

import java.util.ArrayList;

/**
 * Activity After Login
 * Take care of LibraryManager creation
 */
public class MainActivity extends AppCompatActivity {


    private ListView _menu_list;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Configure menu
        _menu_list = (ListView) findViewById(R.id.menu_main);
        ArrayList<String> ts = new ArrayList<>();
        ts.add("Sair");
        _menu_list.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, ts));
        // Set the list's click listener
        _menu_list.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    //Sair
                    new UserPreferences(MainActivity.this).eraseLogin();
                    startActivity(LoginActivity.newIntent(MainActivity.this));
                    finish();
                }
            }
        });

        //Check if LibraryManager exists
        if (!LibraryManager.loaded(savedInstanceState)) {
            //Try to load from savedInstance
            LoginParameters loginParameters = (LoginParameters) getIntent().getSerializableExtra(Util.EXTRA_LOGIN_PARAMETERS);
            if (loginParameters == null) {
                startActivity(LoginActivity.newIntent(this));
                finish();
                return;
            }
            LibraryManager.create(loginParameters);
        }

        /* Load fragment */
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment mainFragment = fragmentManager.findFragmentById(R.id.main_fragment_container);
        //Put right main fragment
        if (mainFragment == null) {
            //Put borrowed books fragment
            mainFragment = new BorrowedBooksFragment();
            fragmentManager.beginTransaction()
                    .add(R.id.main_fragment_container, mainFragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle bundle) {
        super.onSaveInstanceState(bundle);
        LibraryManager.saveInstance(bundle);
    }

    /** INTENT CREATE METHODS **/

    /**
     * Create new intent to StartActivity, used after login
     * @param packageContext previous activity
     * @return
     */
    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, MainActivity.class);
        return intent;
    }

    /**
     * Create new intent to StartActivity, used when user has saved login
     * @param packageContext
     * @param login
     * @return
     */
    public static Intent newIntent(Context packageContext, LoginParameters login) {
        Intent intent = new Intent(packageContext, MainActivity.class);
        intent.putExtra(Util.EXTRA_LOGIN_PARAMETERS, login);
        return intent;
    }
}
