package com.book.renew.renovae.activity;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.book.renew.renovae.R;
import com.book.renew.renovae.library.ILibrary;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;
import com.book.renew.renovae.library.exception.UnknownLoginException;
import com.book.renew.renovae.library.impl.Universities;
import com.book.renew.renovae.util.LoginParameters;
import com.book.renew.renovae.util.UserPreferences;
import com.book.renew.renovae.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private EditText _usernameEdit;
    private EditText _passwordEdit;
    private Spinner _universitiesSpinner;
    private CheckBox _rememberMeCheckbox;

    private UserPreferences _preferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Button _loginButton = (Button) findViewById(R.id.button_login);
        _usernameEdit = (EditText) findViewById(R.id.edit_username);
        _passwordEdit = (EditText) findViewById(R.id.edit_password);
        _universitiesSpinner = (Spinner) findViewById(R.id.spinner_universities);
        _rememberMeCheckbox = (CheckBox) findViewById(R.id.checkbox_remember_me);

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String university = String.valueOf(_universitiesSpinner.getSelectedItem());
                String username = _usernameEdit.getText().toString();
                String password = _passwordEdit.getText().toString();
                boolean save = _rememberMeCheckbox.isChecked();

                if (!Universities.instance().hasUniversity(university)) {
                    displayMessage(getString(R.string.university_missing));
                    _universitiesSpinner.requestFocus();
                    return;
                }
                if (username.equals("")) {
                    displayMessage(getString(R.string.username_missing));
                    _usernameEdit.requestFocus();
                    return;
                }
                if (password.equals("")) {
                    displayMessage(getString(R.string.password_missing));
                    _passwordEdit.requestFocus();
                }
                (new LoginTask()).execute(new LoginParameters(username, password, university, save));

            }
        });
        //Create preferences
        _preferences = new UserPreferences(LoginActivity.this);
        //Populate universities spinner
        populateUniversitiesSpinner();

        //Tries to login if needed
        LoginParameters login = (LoginParameters) getIntent().getSerializableExtra(Util.EXTRA_LOGIN_PARAMETERS);
        if (login != null) {
            //Fill field
            _usernameEdit.setText(login.username);
            _passwordEdit.setText(login.password);
            selectSpinnerItemByValue(_universitiesSpinner, login.university);
            (new LoginTask()).execute(new LoginParameters(login.username, login.password, login.university, false));
        }
        else {
            //Set the focus
            _usernameEdit.requestFocus();
        }

    }

    /** INTENT CREATE METHODS **/

    /**
     * Intent for login activity automatically login
     * @param packageContext Current activity
     * @param login parameters to login (or null if do not want to login)
     * @return the Intent
     */
    public static Intent newIntent(Context packageContext, LoginParameters login) {
        Intent intent = new Intent(packageContext, LoginActivity.class);
        intent.putExtra(Util.EXTRA_LOGIN_PARAMETERS, login);
        return intent;
    }

    /** UTIL METHODS FOR UNIVERSITIES SPINNER */

    /**
     * Populate universities spinner with Universties class map
     */
    private void populateUniversitiesSpinner() {
        Set<String> set_universities = Universities.instance().getUniversities();
        List<String> universities = new ArrayList<>(set_universities.size() + 1);
        universities.add("Escolha sua Universidade");
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

    /** UTIL METHODS FOR DISPLAYING MESSAGE */

    private void displayMessage(String message) {
        displayMessage(message, false);
    }

    private void displayMessage(String message, boolean long_duration) {
        Toast.makeText(this, message, long_duration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    /**
     * Async task to do login
     */
    private class LoginTask extends AsyncTask<LoginParameters, Void, String> {

        private ProgressDialog _progress;
        private ILibrary _lib;
        private LoginParameters _param;
        @Override
        protected void onPreExecute() {
            _progress = ProgressDialog.show(LoginActivity.this, "", "Fazendo login...");
        }

        @Override
        protected String doInBackground(LoginParameters... params) {
            try {
                _param = params[0];
                _lib = Universities.instance().getUniversity(_param.university);
                _lib.login(_param.username, _param.password);
               return null;
            } catch (IOException e) {
                return getResources().getString(R.string.io_error);
            } catch (UnexpectedPageContent e) {
                return getResources().getString(R.string.unexpected_content_error);
            } catch (UnknownLoginException e) {
                return getResources().getString(R.string.login_error);
            } catch (LoginException e) {
                return e.getMessage();
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(String result) {
            _progress.dismiss();
            if (result != null)
                displayMessage(result, true);
            else {
                if (_param.save)
                    _preferences.updateLogin(_param);
                startActivity(BorrowedBooksActivity.newIntent(LoginActivity.this, _lib));
                finish();
            }


        }
    }

}
