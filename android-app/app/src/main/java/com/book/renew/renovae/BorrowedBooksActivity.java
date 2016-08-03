package com.book.renew.renovae;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.book.renew.renovae.library.IBorrow;
import com.book.renew.renovae.library.ILibrary;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;
import com.book.renew.renovae.utils.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BorrowedBooksActivity extends AppCompatActivity {

    private ILibrary _lib = null;
    private List<IBorrow> _borrows = null;

    private ListView _borrowsListView;

    private static final String KEY_LIBRARY = "library";
    private static final String KEY_BORROWS = "borrows";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowed_books);

        _borrowsListView = (ListView) findViewById(R.id.list_view_borrows);

        if (savedInstanceState != null) {
            //Found saved instance
            _lib = (ILibrary) savedInstanceState.getSerializable(KEY_LIBRARY);
            IBorrow[] tempBorrows = (IBorrow[]) savedInstanceState.getSerializable(KEY_BORROWS);
            if (tempBorrows != null) {
                _borrows = Arrays.asList(tempBorrows);
            }
        }

        //Get from intent
        if (_lib == null)
            _lib = (ILibrary) getIntent().getSerializableExtra(Util.EXTRA_LIBRARY);
        if (_borrows == null)
            new BorrowsTask().execute();
        else {
            showBorrows();
        }



    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //Save stuff
        savedInstanceState.putSerializable(KEY_LIBRARY, _lib);
        if (_borrows != null)
            savedInstanceState.putSerializable(KEY_BORROWS, _borrows.toArray(new IBorrow[_borrows.size()]));
        else
            savedInstanceState.putSerializable(KEY_BORROWS, null);
    }

    public static Intent newIntent(Context packageContext, ILibrary library) {
        Intent intent = new Intent(packageContext, BorrowedBooksActivity.class);
        intent.putExtra(Util.EXTRA_LIBRARY, library);
        return intent;
    }

    private class BorrowsTask extends AsyncTask<Void, Void, List<IBorrow>> {

        private boolean _save;
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected List<IBorrow> doInBackground(Void... params) {
            try {
                return _lib.getBorrowedBooks();
            } catch (IOException e) {
                return null;
            } catch (UnexpectedPageContent unexpectedPageContent) {
                return null;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(List<IBorrow> result) {
            _borrows = result;
            showBorrows();
        }
    }

    private void showBorrows() {
        if (_borrows == null)
            displayMessage("Nenhum empréstimo encontrado");
        else {
            displayMessage("Você tem " + _borrows.size() + " empréstimo(s)");
            //Add borrows to recycler view
            ArrayAdapter adapter = new ArrayAdapter<Object>(this, R.layout.activity_borrow_item, _borrows.toArray());
            _borrowsListView.setAdapter(adapter);

        }
    }

    private void displayMessage(String message) {
        displayMessage(message, false);
    }

    private void displayMessage(String message, boolean long_duration) {
        Toast.makeText(this, message, long_duration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }
}
