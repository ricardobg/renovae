package com.book.renew.renovae;

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

import com.book.renew.renovae.library.IBorrow;
import com.book.renew.renovae.library.ILibrary;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;
import com.book.renew.renovae.library.impl.Universities;
import com.book.renew.renovae.library.impl.usp.UspLibrary;
import com.book.renew.renovae.utils.web.Page;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LoginActivity extends AppCompatActivity {

    private EditText _usernameEdit;
    private EditText _passwordEdit;
    private Button _loginButton;
    private Spinner _universitiesSpinner;
    private CheckBox _rememberMeCheckbox;
    private LoginTask _making_login = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _loginButton = (Button) findViewById(R.id.button_login);
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
                    displayMessage("Escolha uma universidade!");
                    _universitiesSpinner.requestFocus();
                    return;
                }
                if (username.equals("")) {
                    displayMessage("Digite o usuário");
                    _usernameEdit.requestFocus();
                    return;
                }
                if (password.equals("")) {
                    displayMessage("Digite a senha");
                    _passwordEdit.requestFocus();
                }
                if (_making_login == null) {
                    _making_login = new LoginTask();
                    _making_login.execute(new LoginParameters(username, password, university, save));
                }
            }
        });

        //Populate universities spinner
        populateUniversitiesSpinner();

        //Set the focus
        _usernameEdit.requestFocus();

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

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

    private class LoginParameters {
        private final String username;
        private final String password;
        private final String university;
        private final boolean save;

        public LoginParameters(String username, String password, String university, boolean save) {
            this.username = username;
            this.password = password;
            this.university = university;
            this.save = save;
        }
    }

    /**
     * Async task to do login
     */
    private class LoginTask extends AsyncTask<LoginParameters, Void, String> {

        private boolean _save;
        private ILibrary _lib;
        @Override
        protected void onPreExecute() {
            displayMessage("Fazendo login...");
        }

        @Override
        protected String doInBackground(LoginParameters... params) {
            try {
                LoginParameters param = params[0];
                _lib = Universities.instance().getUniversity(param.university);
                _lib.login(param.username, param.password);
                _save = param.save;
               return null;
            } catch (IOException e) {
                return ("Erro de conexão: " + e.getMessage());
            } catch (UnexpectedPageContent e) {
                return ("Página com erro: " + e.getMessage());
            } catch (LoginException e) {
                return (e.getMessage());
            }

        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null)
                displayMessage(result, true);
            else {
                //TODO: Check _save and save university, username and password
                startActivity(BorrowedBooksActivity.newIntent(LoginActivity.this, _lib));
            }
            _making_login = null;
        }
    }

    private void displayMessage(String message) {
        displayMessage(message, false);
    }

    private void displayMessage(String message, boolean long_duration) {
        Toast.makeText(this, message, long_duration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }
}
