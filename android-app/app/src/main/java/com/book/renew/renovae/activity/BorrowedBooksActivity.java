package com.book.renew.renovae.activity;

import android.content.Context;
import android.content.Intent;
import android.media.Image;
import android.os.AsyncTask;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.book.renew.renovae.R;
import com.book.renew.renovae.library.IBorrow;
import com.book.renew.renovae.library.ILibrary;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.RenewException;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;
import com.book.renew.renovae.library.exception.renew.CantExtendRenewException;
import com.book.renew.renovae.library.impl.Universities;
import com.book.renew.renovae.util.LoginParameters;
import com.book.renew.renovae.util.UserPreferences;
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

    private UserPreferences _user_prefs;

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

        _user_prefs = new UserPreferences(BorrowedBooksActivity.this);

        //Check for saved instance
        if (savedInstanceState != null) {
            _lib = (ILibrary) savedInstanceState.getSerializable(KEY_LIBRARY);
            IBorrow[] tempBorrows = (IBorrow[]) savedInstanceState.getSerializable(KEY_BORROWS);
            if (tempBorrows != null) {
                _borrows = new ArrayList<>(Arrays.asList(tempBorrows));
            }
        }

        //If didn't find lib, get from intent
        if (_lib == null) {
            _lib = (ILibrary) getIntent().getSerializableExtra(Util.EXTRA_LIBRARY);
            //Not logged in
            if (_lib == null) {
            }
        }
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
     * Create new intent to this activity, used after login
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
        private TextView _titleText;
        private TextView _authorText;
        private TextView _dueDateText;
        private ImageView _renew_button;
        private IBorrow _borrow;

        public BorrowsViewHolder(View itemView) {
            super(itemView);
            _titleText = (TextView) itemView.findViewById(R.id.borrow_book_title);
            _authorText = (TextView) itemView.findViewById(R.id.borrow_book_authors);
            _dueDateText = (TextView) itemView.findViewById(R.id.borrow_due_date);
            _renew_button = (ImageView) itemView.findViewById(R.id.renew_button);

            _renew_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (_borrow != null)
                        new RenewTask().execute(_borrow);
                }
            });
        }

        public void setBorrow(IBorrow borrow) {
            this._borrow = borrow;
            setTitle(borrow.getBook().getTitle());
            setAuthors(borrow.getBook().getAuthors());
            setDueDate(borrow.getDueDate());

        }

        private void setTitle(String title) {_titleText.setText(title);
        }
        private void setAuthors(String authors) {
            _authorText.setText(authors);
        }
        private void setDueDate(Date dueDate) {
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
            holder.setBorrow(borrow);
        }

        @Override
        public int getItemCount() {
            return _borrows.size();
        }
    }

    /**
     * Task to load borrows
     */
    private class BorrowsTask extends AsyncTask<LoginParameters, Void, Exception> {
        private ArrayList<IBorrow> _borrows = null;
        @Override
        protected void onPreExecute() {
        }

        private void doLogin() throws UnexpectedPageContent, LoginException, IOException {
            if (_lib == null) {
                LoginParameters login = _user_prefs.getLogin();
                _lib = Universities.instance().getUniversity(login.university);
                _lib.login(login.username, login.password);
            }
        }

        @Override
        protected Exception doInBackground(LoginParameters... params) {
            try {
                doLogin();
                _borrows = _lib.getBorrowedBooks();
                return null;
            } catch (LogoutException e) {
                return e;
            } catch (IOException e) {
                return e;
            } catch (LoginException e) {
                return e;
            } catch (UnexpectedPageContent e) {
                return e;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(Exception error) {
            _borrowsSwipeRefresh.setRefreshing(false);
            //Compare and add new

            if (_borrows != null) {
                int len = 0;
                len = _borrows.size();
                displayMessage("Você tem " + len + " empréstimo(s)");
                updateBorrowsRecycler(_borrows);
            }
            else {
                if (error instanceof LogoutException)
                    displayMessage("Deu logout!");
                else if (error instanceof UnexpectedPageContent)
                    displayMessage("Erro na página");
                else if (error instanceof  IOException)
                    displayMessage("Erro de conexão");
                else {
                    if (error instanceof LoginException) {
                        //Volta para tela de login
                        startActivity(LoginActivity.newIntent(BorrowedBooksActivity.this, error.getMessage()));
                        finish();
                    }
                    else
                        updateBorrowsRecycler(_borrows);
                }
            }



        }
    }



    private class RenewTask extends AsyncTask<IBorrow, Void, Exception> {
        private IBorrow _borrow = null;
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected Exception doInBackground(IBorrow... params) {
            try {
                _borrow = params[0];
                _borrow.renew();
                return null;
            } catch (LogoutException e) {
                return e;
            } catch (Exception e) {
                return e;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(Exception error) {
            if (error == null) {
                //Atualiza empréstimos
                //TODO: melhorar
                displayMessage("Livro renovado!");
                _borrowsSwipeRefresh.post(new Runnable() {
                    @Override
                    public void run() {
                        _borrowsSwipeRefresh.setRefreshing(true);
                    }
                });
                (new BorrowsTask()).execute();
            }
            else {
                error.printStackTrace();
                if (error instanceof LogoutException)
                    displayMessage("Deu logout!");
                else if (error instanceof UnexpectedPageContent)
                    displayMessage("Erro na página");
                else if (error instanceof  IOException)
                    displayMessage("Erro de conexão");
                else if (error instanceof RenewException)
                    displayMessage("Erro: " + error.getMessage());
            }
        }
    }
}
