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
import android.widget.TextView;
import android.widget.Toast;

import com.book.renew.renovae.R;
import com.book.renew.renovae.library.LibraryManager;
import com.book.renew.renovae.library.exception.InvalidUniversityException;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;
import com.book.renew.renovae.library.exception.UnknownLoginException;
import com.book.renew.renovae.library.LoginParameters;
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
        _rememberMeCheckbox = (CheckBox) findViewById(R.id.checkbox_remember_me);
        _errorTextView = (TextView) findViewById(R.id.text_login_error);

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String university = String.valueOf(_universitiesSpinner.getSelectedItem());
                String username = _usernameEdit.getText().toString();
                String password = _passwordEdit.getText().toString();
                boolean save = _rememberMeCheckbox.isChecked();

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
                (new LoginTask()).execute(new LoginParameters(username, password, university, save));

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
            _rememberMeCheckbox.setChecked(true);
            //(new LoginTask()).execute(new LoginParameters(login.username, login.password, login.university, false));
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
    private class LoginTask extends AsyncTask<LoginParameters, Void, String> {

        private ProgressDialog _progress;
        private LibraryManager _lib;
        @Override
        protected void onPreExecute() {
            _progress = ProgressDialog.show(LoginActivity.this, "", getString(R.string.login_in_progress));
        }

        @Override
        protected String doInBackground(LoginParameters... params) {
            try {
                _lib = new LibraryManager(params[0], true);
               return null;
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println(e.getMessage());
                return getString(R.string.io_error);
            } catch (UnexpectedPageContent e) {
                return getString(R.string.unexpected_content_error);
            } catch (UnknownLoginException e) {
                return getString(R.string.login_error);
            } catch (LoginException e) {
                return e.getMessage();
            } catch (InvalidUniversityException e) {
                return getString(R.string.invalid_university);
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(String result) {
            _progress.dismiss();
            if (result != null)
                displayErrorMessage(result);
            else {
                if (_lib.getLoginParams().save)
                    _preferences.updateLogin(_lib.getLoginParams());
                startActivity(BorrowedBooksActivity.newIntent(LoginActivity.this, _lib));
                finish();
            }
        }
    }
    public void handleException(Exception error) {
        if (error instanceof UnexpectedPageContent)
            displayErrorMessage("Erro na página");
        else if (error instanceof IOException) {
            if (!Util.hasInternetConnection(LoginActivity.this))
                displayErrorMessage("Sem conexão com a internet");
            else
                displayErrorMessage("Site da biblioteca indisponível");
        }
        else if (error instanceof UnknownLoginException)
            displayErrorMessage("Erro na página");
        else if (error instanceof LoginException)
            displayErrorMessage(error.getMessage());

    }

}
