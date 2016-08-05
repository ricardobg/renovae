package com.book.renew.renovae.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.book.renew.renovae.R;
import com.book.renew.renovae.library.IBorrow;
import com.book.renew.renovae.library.ILibrary;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;
import com.book.renew.renovae.util.Util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BorrowedBooksActivity extends AppCompatActivity {

    private ILibrary _lib = null;
    private List<IBorrow> _borrows = null;

    private RecyclerView _borrowsRecyclerView;
    private SwipeRefreshLayout _borrowsSwipeRefresh;

    private BorrowsAdapter _borrowsAdapter;

    private static final String KEY_LIBRARY = "library";
    private static final String KEY_BORROWS = "borrows";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowed_books);

        _borrowsRecyclerView = (RecyclerView) findViewById(R.id.recycler_view_borrows);
        _borrowsSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_borrows);


        _borrowsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        _borrowsSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                System.out.println("Called on refresh");
                (new BorrowsTask()).execute();
            }
        });

        //Check for saved instance
        if (savedInstanceState != null) {
            _lib = (ILibrary) savedInstanceState.getSerializable(KEY_LIBRARY);
            IBorrow[] tempBorrows = (IBorrow[]) savedInstanceState.getSerializable(KEY_BORROWS);
            if (tempBorrows != null) {
                _borrows = Arrays.asList(tempBorrows);
            }
        }

        //If didn't find lib, get from intent
        if (_lib == null)
            _lib = (ILibrary) getIntent().getSerializableExtra(Util.EXTRA_LIBRARY);
        if (_borrows == null) {
            _borrowsSwipeRefresh.post(new Runnable() {
                @Override
                public void run() {
                    _borrowsSwipeRefresh.setRefreshing(true);
                }
            });
            (new BorrowsTask()).execute();
        }
        else
            showBorrows();



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

    /** INTENT CREATE METHODS **/

    /**
     * Create new intent to this activity
     * @param packageContext previous activity
     * @param library the library to send to this page
     * @return
     */
    public static Intent newIntent(Context packageContext, ILibrary library) {
        Intent intent = new Intent(packageContext, BorrowedBooksActivity.class);
        intent.putExtra(Util.EXTRA_LIBRARY, library);
        return intent;
    }


    /**
     * Just show all borrows on screen
     */
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

    /**
     * Compares each borrow with the previous list
     * @param newBorrows new borrows list
     */
    private void showBorrows(List<IBorrow> newBorrows) {
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

    private class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView _titleView;
        public TextView _authorView;
        public TextView _dueDateView;

        public ViewHolder(TextView titleView, TextView authorView, TextView dueDateView) {
            super(titleView, authorView, dueDateView);
            mTextView = v;
        }
    }

    private class BorrowsAdapter extends RecyclerView.Adapter<ViewHolder> {



        private ArrayList<IBorrow> _borrows;

        public BorrowsAdapter(ArrayList<IBorrow> borrows) {
            this._borrows = borrows;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }

    /**
     * Task to load borrows
     */
    private class BorrowsTask extends AsyncTask<Void, Void, List<IBorrow>> {
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
            _borrowsSwipeRefresh.setRefreshing(false);
            showBorrows(result);
        }
    }
}
