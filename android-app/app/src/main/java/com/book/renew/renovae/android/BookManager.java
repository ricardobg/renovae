package com.book.renew.renovae.android;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.impl.Book;
import com.book.renew.renovae.library.util.web.Page;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Singleton to manage books
 */
public class BookManager {

    private static BookManager _manager = null;
    private BookManager() {

    }

    public static BookManager get() {
        if (_manager == null)
            _manager = new BookManager();
        return _manager;
    }

    /**
     * Loading book info listener
     */
    public interface OnLoadListener {
        void onFinished(Book book);
        void onStartCoverLoading(Book book);
    }
    /**
     * Receives a book and updates its info using either the local database
     * or the Google Books API
     * @param book
     * @throws NetworkException
     */
    public void loadBookInfo(Book book, OnLoadListener listener) throws NetworkException {
        if (book.getIsbn() != null) {
            //Call google Book API
            Page bookInfo = new Page("https://www.googleapis.com/books/v1/volumes?q=isbn:" + book.getIsbn());
            try {
                JSONObject json = new JSONObject(bookInfo.getContent());
                if (json.getInt("totalItems") != 0) {
                    //Get first book
                    JSONObject jsonBook = json.getJSONArray("items")
                            .getJSONObject(0)
                            .getJSONObject("volumeInfo");

                    //Try to get book title
                    String title = jsonBook.optString("title");
                    if (!title.equals("")) {
                        String subtitle = jsonBook.optString("subtitle");
                        if (!subtitle.equals(""))
                            title += ": " + subtitle;
                        book.setTitle(title);
                    }
                    //Try to get book Authors
                    JSONArray authorsArray = jsonBook.optJSONArray("authors");
                    if (authorsArray != null && authorsArray.length() > 0) {
                        String authors = authorsArray.getString(0);
                        for (int i = 1; i < authorsArray.length(); i++)
                            authors += ", " + authorsArray.getString(i);
                        book.setAuthors(authors);
                    }

                    //Try to get image link
                    String imageLink = jsonBook
                            .getJSONObject("imageLinks")
                            .getString("thumbnail");
                    //Call listener
                    listener.onStartCoverLoading(book);
                    URL url = new URL(imageLink);
                    BufferedInputStream stream = new BufferedInputStream(url.openStream());
                    Bitmap bMap = BitmapFactory.decodeStream(stream);
                    if (stream != null)
                        stream.close();
                    book.setCover(bMap);
                }
            } catch (JSONException e) {
                //Silent fault
            } catch (MalformedURLException e) {
                //Silent fault
            } catch (IOException e) {
                throw new NetworkException("Erro na rede");
            }
        }

        listener.onFinished(book);
    }

}
