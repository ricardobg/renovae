package com.book.renew.renovae.activity;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.Date;
import java.util.List;

public class BorrowedBooksActivity extends AppCompatActivity {

    private ILibrary _lib = null;
    private ArrayList<IBorrow> _borrows = null;

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
                _borrows = new ArrayList<>(Arrays.asList(tempBorrows));
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
        else {
            _borrowsAdapter = new BorrowsAdapter(_borrows);
            _borrowsRecyclerView.setAdapter(_borrowsAdapter);
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

    private void displayMessage(String message) {
        displayMessage(message, false);
    }

    private void displayMessage(String message, boolean long_duration) {
        Toast.makeText(this, message, long_duration ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
    }

    private void updateBorrowsRecycler(ArrayList<IBorrow> newBorrows) {
        if (newBorrows == null) {
            if (_borrows.size() > 0) {
                _borrowsAdapter.notifyItemRangeRemoved(0, _borrows.size());
                _borrows.clear();
            }
        }
        else {
            //TODO: improve this code
            if (_borrows == null) {
                _borrows = newBorrows;
                _borrowsAdapter = new BorrowsAdapter(_borrows);
                _borrowsRecyclerView.setAdapter(_borrowsAdapter);
            }
            else {
                _borrows.clear();
                for (int i = 0; i < newBorrows.size(); i++)
                    _borrows.add(newBorrows.get(i));
                _borrowsAdapter.notifyDataSetChanged();
            }
        }
    }

    private class BorrowsViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView _titleText;
        public TextView _authorText;
        public TextView _dueDateText;

        public BorrowsViewHolder(View itemView) {
            super(itemView);
            _titleText = (TextView) itemView.findViewById(R.id.borrow_book_title);
            _authorText = (TextView) itemView.findViewById(R.id.borrow_book_authors);
            _dueDateText = (TextView) itemView.findViewById(R.id.borrow_due_date);
        }

        public void setTitle(String title) {
            _titleText.setText(title);
        }

        public void setAuthors(String authors) {
            _authorText.setText(authors);
        }

        public void setDueDate(Date dueDate) {
            _dueDateText.setText(Util.FULL_YEAR_DATE_FORMAT.format(dueDate));
        }

    }

    private class BorrowsAdapter extends RecyclerView.Adapter<BorrowsViewHolder> {
        private ArrayList<IBorrow> _borrows;

        public BorrowsAdapter(ArrayList<IBorrow> borrows) {
            this._borrows = borrows;
        }

        @Override
        public BorrowsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View borrowView = inflater.inflate(R.layout.activity_borrow_item, parent, false);
            return new BorrowsViewHolder(borrowView);
        }

        @Override
        public void onBindViewHolder(BorrowsViewHolder holder, int position) {
            IBorrow borrow = _borrows.get(position);

            holder.setTitle(borrow.getBook().getTitle());
            holder.setAuthors(borrow.getBook().getAuthors());
            holder.setDueDate(borrow.getDueDate());
        }

        @Override
        public int getItemCount() {
            return _borrows.size();
        }
    }

    /**
     * Task to load borrows
     */
    private class BorrowsTask extends AsyncTask<Void, Void, ArrayList<IBorrow>> {
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected ArrayList<IBorrow> doInBackground(Void... params) {
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
        protected void onPostExecute(ArrayList<IBorrow> result) {
            _borrowsSwipeRefresh.setRefreshing(false);
            //Compare and add new
            int len = 0;
            if (result != null)
                len = result.size();
            displayMessage("Você tem " + len + " empréstimo(s)");
            updateBorrowsRecycler(result);

        }
    }
}
