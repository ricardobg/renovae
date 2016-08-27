package com.book.renew.renovae.view.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.book.renew.renovae.R;
import com.book.renew.renovae.android.LibraryManager;
import com.book.renew.renovae.android.Util;
import com.book.renew.renovae.library.exception.DefaultMessageException;
import com.book.renew.renovae.android.LoginParameters;
import com.book.renew.renovae.android.UserPreferences;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private EditText _usernameEdit;
    private EditText _passwordEdit;
    private Spinner _universitiesSpinner;
    private TextView _errorTextView;


    private UserPreferences _preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button _loginButton = (Button) findViewById(R.id.button_login);
        _usernameEdit = (EditText) findViewById(R.id.edit_username);
        _passwordEdit = (EditText) findViewById(R.id.edit_password);
        _universitiesSpinner = (Spinner) findViewById(R.id.spinner_universities);
        _errorTextView = (TextView) findViewById(R.id.text_login_error);

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String university = String.valueOf(_universitiesSpinner.getSelectedItem());
                String username = _usernameEdit.getText().toString();
                String password = _passwordEdit.getText().toString();

                if (!LibraryManager.universityExists(university)) {
                    displayErrorMessage(getString(R.string.university_missing));
                    _universitiesSpinner.requestFocus();
                    return;
                }
                if (username.equals("")) {
                    displayErrorMessage(getString(R.string.username_missing));
                    _usernameEdit.requestFocus();
                    return;
                }
                if (password.equals("")) {
                    displayErrorMessage(getString(R.string.password_missing));
                    _passwordEdit.requestFocus();
                }
                (new LoginTask()).execute(new LoginParameters(username, password, university));

            }
        });
        //Create preferences
        _preferences = new UserPreferences(LoginActivity.this);
        //Populate universities spinner
        populateUniversitiesSpinner();

        //Shows error message
        String error_message = (String) getIntent().getSerializableExtra(Util.EXTRA_ERROR_MESSAGE);
        if (error_message != null)
            displayErrorMessage(error_message);

        LoginParameters login = _preferences.getLogin();
        if (login != null) {
            //Fill field
            _usernameEdit.setText(login.username);
            _passwordEdit.setText(login.password);
            selectSpinnerItemByValue(_universitiesSpinner, login.university);
        }
        else {
            //Set the focus
            _usernameEdit.requestFocus();
        }

    }

    /** INTENT CREATE METHODS **/

    /**
     * Intent for login error
     * @param packageContext Current activity
     * @param error parameters to login (or null if do not want to login)
     * @return the Intent
     */
    public static Intent newIntent(Context packageContext, String error) {
        Intent intent = new Intent(packageContext, LoginActivity.class);
        intent.putExtra(Util.EXTRA_ERROR_MESSAGE, error);
        return intent;
    }

    /**
     * Intent for login page
     * @param packageContext
     * @return
     */
    public static Intent newIntent(Context packageContext) {
        Intent intent = new Intent(packageContext, LoginActivity.class);
        return intent;
    }

    /** UTIL METHODS FOR UNIVERSITIES SPINNER */

    /**
     * Populate universities spinner with Universties class map
     */
    private void populateUniversitiesSpinner() {
        Set<String> set_universities = LibraryManager.getUniversities();
        List<String> universities = new ArrayList<>(set_universities.size() + 1);
        universities.add(getString(R.string.university_choice));
        for (String s : set_universities)
            universities.add(s);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                universities);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        _universitiesSpinner.setAdapter(adapter);
    }

    /**
     * Select a value in the spinner that is equal to the passed value
     * @param spinner Spinner to select
     * @param value to compare
     */
    private void selectSpinnerItemByValue(Spinner spinner, String value) {
        ArrayAdapter adapter = (ArrayAdapter) spinner.getAdapter();
        for (int position = 0; position < adapter.getCount(); position++) {
            if (adapter.getItem(position).equals(value)) {
                spinner.setSelection(position);
                return;
            }
        }
    }


    private void displayErrorMessage(String message) {
        _errorTextView.setText(message);
    }

    /**
     * Async task to do login
     */
    private class LoginTask extends AsyncTask<LoginParameters, Void, DefaultMessageException> {

        private ProgressDialog _progress;
        @Override
        protected void onPreExecute() {
            _progress = ProgressDialog.show(LoginActivity.this, "", getString(R.string.login_in_progress));
        }

        @Override
        protected DefaultMessageException doInBackground(LoginParameters... params) {
            try {
                LibraryManager.createAndLogin(params[0]);
               return null;
            } catch (DefaultMessageException e) {
                return e;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(DefaultMessageException error) {
            _progress.dismiss();
            if (error != null)
                displayErrorMessage(error.getMessage());
            else {
                _preferences.updateLogin(LibraryManager.get().getLoginParams());
                startActivity(MainActivity.newIntent(LoginActivity.this));
                finish();
            }
        }
    }

}

