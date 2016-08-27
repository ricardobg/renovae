package com.book.renew.renovae.library.impl;

import android.graphics.Bitmap;
import java.io.Serializable;

/**
 * Class that represents a book that
 */
public class Book implements Serializable, Comparable<Book> {
    private String _isbn = null;
    private String _title;
    private String _authors;
    private transient Bitmap _cover;

    public String getIsbn() {
        return _isbn;
    }
    public String getTitle() {
        return _title;
    }
    public String getAuthors() {
        return _authors;
    }
    public Bitmap getCover() { return _cover; }

    public Book(String title, String authors) {
        _title = title;
        _authors = authors;
    }

    private static String convertAuthorName(String name) {
        String result = "";
        String[] names = name.split(" ");
        if (names.length <= 2)
            return name;
        result += names[0] + " ";
        for (int i = 1; i < names.length - 1; i++)
            result += " " + names[i].substring(0, 1) + ".";
        result += " " + names[names.length - 1];
        return result;

    }

    public void setIsbn(String isbn) {
        _isbn = isbn;
    }
    public void setTitle(String title) {
        _title = title;
    }
    public void setAuthors(String authors) {
        _authors = authors;
    }
    public void setCover(Bitmap cover) { _cover = cover; }

    @Override
    public String toString() {
        return getTitle() + " - " + getAuthors();
    }

    @Override
    public int compareTo(Book book) {
        int title_cmp = this._title.compareTo(book._title);
        if (title_cmp != 0)
            return title_cmp;

        int authors_cmp = this._authors.compareTo(book._authors);
        if (authors_cmp != 0)
            return authors_cmp;

//        int isbn_cmp = this._isbn.compareTo(book._isbn);
  //      return isbn_cmp;
        return 0;
    }

}
