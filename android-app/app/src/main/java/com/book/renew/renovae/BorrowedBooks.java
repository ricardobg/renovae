package com.book.renew.renovae;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.book.renew.renovae.library.api.ILibrary;
import com.book.renew.renovae.library.api.UnexpectedPageContent;
import com.book.renew.renovae.library.api.usp.UspLibrary;

import java.io.IOException;

public class BorrowedBooks extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_borrowed_books);
        try {
            ILibrary lib = new UspLibrary();
        } catch (UnexpectedPageContent unexpectedPageContent) {
            unexpectedPageContent.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
