package com.book.renew.renovae.view.fragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.book.renew.renovae.R;
import com.book.renew.renovae.android.BookManager;
import com.book.renew.renovae.android.Borrow;
import com.book.renew.renovae.android.LibraryManager;
import com.book.renew.renovae.android.LoginParameters;
import com.book.renew.renovae.library.exception.DefaultMessageException;
import com.book.renew.renovae.library.exception.InvalidUniversityException;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.impl.Book;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.util.Util;
import com.book.renew.renovae.view.activity.LoginActivity;
import com.yqritc.recyclerviewflexibledivider.HorizontalDividerItemDecoration;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

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

        ArrayList<Borrow> borrows = LibraryManager.get().getCachedBorrows();
        boolean borrows_null = borrows == null;
        System.out.println(borrows_null);
        if (borrows_null)
            borrows = new ArrayList<>();

        _borrowsAdapter = new BorrowsAdapter(borrows);
        _borrowsRecyclerView.setAdapter(_borrowsAdapter);
       /* _borrowsRecyclerView.addItemDecoration(
                new HorizontalDividerItemDecoration.Builder(getActivity())
                        .color(Color.rgb(200,200,200))
                     //   .sizeResId(R.dimen.divider)
                     //   .marginResId(R.dimen.leftmargin, R.dimen.rightmargin)
                        .build());*/

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


    private void updateBorrowsRecycler(ArrayList<Borrow> newBorrows) {
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
    private class BorrowViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        private TextView _titleText;
        private TextView _authorText;
        private TextView _dueDateText;
        private Button _renew_button;
        private Borrow _borrow;
        private RelativeLayout _layout;
        private ProgressBar _renewProgressBar;
        private ProgressBar _bookProgressBar;
        private ProgressBar _borrowProgressBar;
        private ImageView _imageBookCover;

        public BorrowViewHolder(View itemView) {
            super(itemView);
            _titleText = (TextView) itemView.findViewById(R.id.borrow_book_title);
            _authorText = (TextView) itemView.findViewById(R.id.borrow_book_authors);
            _dueDateText = (TextView) itemView.findViewById(R.id.borrow_due_date);
            _renew_button = (Button) itemView.findViewById(R.id.myFAB);
            _layout = (RelativeLayout) itemView.findViewById(R.id.relativelayout);
            _renewProgressBar = (ProgressBar) itemView.findViewById(R.id.renew_progress);
            _bookProgressBar = (ProgressBar) itemView.findViewById(R.id.book_cover_progress);
            _borrowProgressBar = (ProgressBar) itemView.findViewById(R.id.borrow_progress);
            _imageBookCover = (ImageView) itemView.findViewById(R.id.book_cover_image);

        }

        public Borrow getBorrow() {
            return _borrow;
        }

        public void showRenewProgress() {
            _renew_button.setVisibility(View.GONE);
            _renewProgressBar.setVisibility(View.VISIBLE);
        }

        public void hideRenewProgress() {
            _renew_button.setVisibility(View.VISIBLE);
            _renewProgressBar.setVisibility(View.GONE);
        }

        public void showBookCoverProgress() {
            _renew_button.setEnabled(false);
            _imageBookCover.setVisibility(View.GONE);
            _bookProgressBar.setVisibility(View.VISIBLE);
        }

        public void hideBookCoverProgress() {
            _renew_button.setEnabled(true);
            _imageBookCover.setVisibility(View.VISIBLE);
            _bookProgressBar.setVisibility(View.GONE);
            //Update cover
            if (_borrow.getBook().getCover() != null)
                _imageBookCover.setImageBitmap(_borrow.getBook().getCover());
        }

        public void showBorrowProgress() {
            _layout.setAlpha(0.3f);
            _renew_button.setEnabled(false);
            _imageBookCover.setVisibility(View.GONE);
            _borrowProgressBar.setVisibility(View.VISIBLE);
        }

        public void hideBorrowProgress() {
            _layout.setAlpha(1);
            _renew_button.setEnabled(true);
            _imageBookCover.setVisibility(View.VISIBLE);
            _borrowProgressBar.setVisibility(View.GONE);

        }


        public void updateBookInfo() {
            setTitle(_borrow.getBook().getTitle());
            setAuthors(_borrow.getBook().getAuthors());
        }

        public void updateCoverImage() {
            if (_borrow.getBook().getCover() != null)
                _imageBookCover.setImageBitmap(_borrow.getBook().getCover());
        }

        public void setBorrow(Borrow borrow) {
            this._borrow = borrow;
           // setTitle(borrow.getBook().getTitle());
           // setAuthors(borrow.getBook().getAuthors());
            setDueDate(borrow.getDueDate());
            if (borrow.canRenew()) {
                SpannableString text = new SpannableString("RenovAE");
                text.setSpan(new ForegroundColorSpan(Color.BLACK), 0, 5, 0);
                text.setSpan(new ForegroundColorSpan(Color.RED), 5, 7, 0);
                // shove our styled text into the Button
                _renew_button.setAlpha(1);
                _renew_button.setText(text, Button.BufferType.SPANNABLE);
                _renew_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (_borrow != null) {
                            new RenewTask(BorrowViewHolder.this).execute();
                        }
                    }
                });
            }
            else {
                SpannableString text = new SpannableString("RenovAE");
                text.setSpan(new ForegroundColorSpan(Color.rgb(90,90,90)), 0, 7, 0);
                _renew_button.setAlpha(.5f);
                _renew_button.setText(text, Button.BufferType.SPANNABLE);
                _renew_button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        displayMessage(_borrow.getCause().getMessage());
                    }
                });
            }

            new BookInfoTask(this).execute();

        }

        private void setTitle(String title) {_titleText.setText(title);
        }
        private void setAuthors(String authors) {
            _authorText.setText(authors);
        }
        private void setDueDate(Date dueDate) {
            SimpleDateFormat dateFormat = new SimpleDateFormat("EEEE");
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            int days = Util.dateDiff(cal.getTime(), dueDate);
            StringBuilder text = new StringBuilder();
            if (days >= 0) {
                text.append("Devolver ");
                if (days == 0) {
                    text.append("hoje");
                    ((GradientDrawable)_layout.getBackground()).setColor(Color.argb(255,103,58,183));
                  //  _layout.setBackgroundColor(Color.argb(30,255,255,0));
                }
                else {
                    ((GradientDrawable)_layout.getBackground()).setColor(Color.argb(Util.min(240, 80 + 50*(days)), 63, 81, 181));
                  //  _layout.setBackgroundColor(Color.argb(Util.min(127, 40*(days)), 0, 200, 0));
                    if (days == 1)
                        text.append("amanhã");
                    else {
                        text.append("em " + days + " dias");
                    }
                }
            }
            else {
                ((GradientDrawable)_layout.getBackground()).setColor(Color.argb(Util.min(240, 60 + 40*(-days)), 200, 0, 0));
                text.append("Prazo venceu ");
                if (days == -1)
                    text.append("ontem");
                else
                    text.append("há " + (-days) + " dias");
            }
            _dueDateText.setText(text.toString());
        }

    }
    /**
     * Adapter of borrows for RecyclerView
     */
    private class BorrowsAdapter extends RecyclerView.Adapter<BorrowViewHolder> {
        private ArrayList<Borrow> _borrows;

        public BorrowsAdapter(ArrayList<Borrow> borrows) {
            this._borrows = new ArrayList<>(borrows);
            Collections.sort(this._borrows);
        }

        public void notifyBorrowChanged(Borrow changed) {
            //First, find changed

            int before =_borrows.indexOf(changed);
            Collections.sort(this._borrows);
            int after =_borrows.indexOf(changed);
            notifyItemMoved(before, after);
            notifyItemChanged(after);
        }

        public void setBorrows(ArrayList<Borrow> borrows) {
            this._borrows.clear();
            for (Borrow b : borrows)
                this._borrows.add(b);
            Collections.sort(this._borrows);
        }
        @Override
        public BorrowViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View borrowView = inflater.inflate(R.layout.activity_borrow_item, parent, false);
            return new BorrowViewHolder(borrowView);
        }

        @Override
        public void onBindViewHolder(BorrowViewHolder holder, int position) {
            Borrow borrow = _borrows.get(position);
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
        private ArrayList<Borrow> _borrows = null;
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
    private class RenewTask extends AsyncTask<Void, Void, DefaultMessageException> {
        private BorrowViewHolder _borrowHolder = null;
        private boolean _invalid_login = false;

        public RenewTask(BorrowViewHolder viewHolder) {
            this._borrowHolder = viewHolder;
        }
        @Override
        protected void onPreExecute() {
            _borrowHolder.showRenewProgress();
        }

        @Override
        protected DefaultMessageException doInBackground(Void... params) {
            try {
                LibraryManager.get().renew(_borrowHolder.getBorrow());
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
            _borrowHolder.hideRenewProgress();
            if (error == null) {
                //Atualiza empréstimos
                //TODO: melhorar
                displayMessage("Livro renovado!");
                _borrowsAdapter.notifyBorrowChanged(_borrowHolder.getBorrow());
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
     * Task to get book info
     */

    private class BookInfoTask extends AsyncTask<Void, Void, Void> {
        private BorrowViewHolder _borrowHolder = null;

        public BookInfoTask(BorrowViewHolder viewHolder) {
            this._borrowHolder = viewHolder;
        }
        @Override
        protected void onPreExecute() {
            _borrowHolder.showBorrowProgress();
        }

        @Override
        protected Void doInBackground(Void... params) {
            try {
                BookManager.get().loadBookInfo(_borrowHolder.getBorrow().getBook(),
                        new BookManager.OnLoadListener() {
                            @Override
                            public void onFinished(Book book) {

                            }
                            @Override
                            public void onStartCoverLoading(Book book) {
                                publishProgress();
                            }
                        });
            } catch (NetworkException e) {

            }
            return null;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            _borrowHolder.updateBookInfo();
            _borrowHolder.hideBorrowProgress();
            _borrowHolder.showBookCoverProgress();
        }

        @Override
        protected void onPostExecute(Void value) {
            _borrowHolder.updateBookInfo();
            _borrowHolder.updateCoverImage();
            _borrowHolder.hideBorrowProgress();
            _borrowHolder.hideBookCoverProgress();
        }
    }


    private void displayMessage(String message) {
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}
