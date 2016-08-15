package com.book.renew.renovae.library.impl;

import java.io.Serializable;

/**
 * Created by ricardo on 27/07/16.
 * Classe que representa um livro
 */
public class Book implements Serializable, Comparable<Book> {
    private String _isbn;
    private String _title;
    private String _authors;
    private int _edition;

    public String getIsbn() {
        return _isbn;
    }
    public String getTitle() {
        return _title;
    }

    public String getAuthors() {
        return _authors;
    }

    public int getEdition() {
        return _edition;
    }

    public Book(String title, String authors) {
        _title = title;
        _authors = authors;
    }

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

        int edition_cmp = this._edition - book._edition;
        if (edition_cmp != 0)
            return edition_cmp;

        int isbn_cmp = this._isbn.compareTo(book._isbn);
        return isbn_cmp;
    }

}
