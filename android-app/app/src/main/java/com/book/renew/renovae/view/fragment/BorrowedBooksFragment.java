package com.book.renew.renovae.view.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.book.renew.renovae.R;
import com.book.renew.renovae.library.LibraryManager;
import com.book.renew.renovae.library.LoginParameters;
import com.book.renew.renovae.library.exception.DefaultMessageException;
import com.book.renew.renovae.library.exception.InvalidUniversityException;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.util.Util;
import com.book.renew.renovae.view.activity.LoginActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class BorrowedBooksFragment extends Fragment {

    private RecyclerView _borrowsRecyclerView;
    private SwipeRefreshLayout _borrowsSwipeRefresh;

    private BorrowsAdapter _borrowsAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_borrowed_books, container, false);
        _borrowsRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_borrows);
        _borrowsSwipeRefresh = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_borrows);
        _borrowsRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        _borrowsSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                (new BorrowsTask()).execute();
            }
        });

        ArrayList<IBorrow> borrows = LibraryManager.get().getCachedBorrows();
        boolean borrows_null = borrows == null;
        System.out.println(borrows_null);
        if (borrows_null)
            borrows = new ArrayList<>();

        _borrowsAdapter = new BorrowsAdapter(borrows);
        _borrowsRecyclerView.setAdapter(_borrowsAdapter);

        if (borrows_null) {
            _borrowsSwipeRefresh.post(new Runnable() {
                @Override
                public void run() {
                    _borrowsSwipeRefresh.setRefreshing(true);
                    (new BorrowsTask()).execute();
                }
            });
        }

        getActivity().setTitle("Empréstimos");
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }


    private void updateBorrowsRecycler(ArrayList<IBorrow> newBorrows) {
        if (newBorrows == null) {
            newBorrows = new ArrayList<>();
        }
        _borrowsAdapter.setBorrows(newBorrows);
        _borrowsAdapter.notifyDataSetChanged();
    }



    /**
     * Adapters
     */

    /**
     * View Holder of a borrow
     */
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
    /**
     * Adapter of borrows for RecyclerView
     */
    private class BorrowsAdapter extends RecyclerView.Adapter<BorrowsViewHolder> {
        private ArrayList<IBorrow> _borrows;

        public BorrowsAdapter(ArrayList<IBorrow> borrows) {
            this._borrows = new ArrayList<>(borrows);
            Collections.sort(this._borrows);
        }

        public void setBorrows(ArrayList<IBorrow> borrows) {
            this._borrows.clear();
            for (IBorrow b : borrows)
                this._borrows.add(b);
            Collections.sort(this._borrows);
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
     * Assync Tasks
     */

    /**
     * Task to load borrows
     */
    private class BorrowsTask extends AsyncTask<LoginParameters, Void, DefaultMessageException> {
        private ArrayList<IBorrow> _borrows = null;
        private boolean _invalid_login = false;

        @Override
        protected DefaultMessageException doInBackground(LoginParameters... params) {
            try {
                _borrows = LibraryManager.get().getBorrowedBooks(true);
                return null;
            }
            catch (LoginException e) {
                _invalid_login = true;
                return e;
            }
            catch (InvalidUniversityException e) {
                _invalid_login = true;
                return e;
            }
            catch (DefaultMessageException e) {
                return e;
            }

        }

        @Override
        protected void onPostExecute(DefaultMessageException error) {
            _borrowsSwipeRefresh.setRefreshing(false);
            //Compare and add new
            if (_borrows != null) {
                updateBorrowsRecycler(_borrows);
            }
            else {
                if (_invalid_login) {
                    startActivity(LoginActivity.newIntent(getActivity(), error.getMessage()));
                    getActivity().finish();
                }
                else {
                    displayMessage(error.getMessage());
                }
            }
        }
    }

    /**
     * Task to renew borrow
     */
    private class RenewTask extends AsyncTask<IBorrow, Void, DefaultMessageException> {
        private IBorrow _borrow = null;
        private boolean _invalid_login = false;
        @Override
        protected void onPreExecute() {
        }

        @Override
        protected DefaultMessageException doInBackground(IBorrow... params) {
            try {
                LibraryManager.get().renew(params[0]);
                return null;
            }  catch (LoginException e) {
                _invalid_login = true;
                return e;
            }
            catch (InvalidUniversityException e) {
                _invalid_login = true;
                return e;
            }
            catch (DefaultMessageException e) {
                return e;
            }
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        @Override
        protected void onPostExecute(DefaultMessageException error) {
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
                if (_invalid_login) {
                    startActivity(LoginActivity.newIntent(getActivity(), error.getMessage()));
                    getActivity().finish();
                }
                else {
                    displayMessage(error.getMessage());
                }
            }
        }
    }


    private void displayMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
