package com.book.renew.renovae;

import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.book.renew.renovae.library.IBorrow;
import com.book.renew.renovae.library.ILibrary;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;
import com.book.renew.renovae.library.impl.usp.UspLibrary;
import com.book.renew.renovae.utils.web.Page;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {

    private EditText _usernameEdit;
    private EditText _passwordEdit;
    private  Button _loginButton;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        _loginButton = (Button) findViewById(R.id.button_login);
        _usernameEdit = (EditText) findViewById(R.id.edit_username);
        _passwordEdit = (EditText) findViewById(R.id.edit_password);

        _loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = _usernameEdit.getText().toString();
                String password = _passwordEdit.getText().toString();
                if (username.equals("")) {
                    displayMessage("Digite o usuário");
                    _usernameEdit.requestFocus();
                    return;
                }
                if (password.equals("")) {
                    displayMessage("Digite a senha");
                    _passwordEdit.requestFocus();
                }
                //TODO: Combobox
                new LoginTask().execute(username, password);
            }
        });
        //Set the focus
        _usernameEdit.requestFocus();
    }

    /**
     * Async task to do login
     */
    private class LoginTask extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            displayMessage("Fazendo login...");
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                ILibrary lib = new UspLibrary();
                lib.login(strings[0], strings[1]);
            } catch (IOException e) {
                return ("Erro de conexão: " + e.getMessage());
            } catch (UnexpectedPageContent e) {
                return ("Página com erro: " + e.getMessage());
            } catch (LoginException e) {
                return (e.getMessage());
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null)
                displayMessage(result);
            else {
                displayMessage("Login efetuado!");
                //TODO: Change view and save
            }
        }
    }

    private void displayMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}
