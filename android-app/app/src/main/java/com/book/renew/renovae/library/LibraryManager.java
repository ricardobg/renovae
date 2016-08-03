package com.book.renew.renovae.library;

import com.book.renew.renovae.library.impl.Universities;

/**
 * Created by ricardo on 03/08/16.
 */
public class LibraryManager {
    private static LibraryManager _instance = null;

    public static LibraryManager instance() {
        return _instance;
    }

    public static LibraryManager instance(String university) {
        _instance = new LibraryManager(university);
        return _instance;
    }

    private ILibrary _library;

    private LibraryManager(String university) {
        _library = Universities.instance().getUniversity(university);
    }

    public ILibrary lib() {
        return _library;
    }
}
