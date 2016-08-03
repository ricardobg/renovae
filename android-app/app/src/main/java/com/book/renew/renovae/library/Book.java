package com.book.renew.renovae.library;

import java.io.Serializable;

/**
 * Created by ricardo on 27/07/16.
 * Classe que representa um livro
 */
public class Book implements Serializable {
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

}
