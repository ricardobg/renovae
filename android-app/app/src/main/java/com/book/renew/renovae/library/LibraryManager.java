package com.book.renew.renovae.library;

import com.book.renew.renovae.library.exception.InvalidUniversityException;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.RenewException;
import com.book.renew.renovae.library.exception.UnexpectedPageContent;
import com.book.renew.renovae.library.exception.UnknownLoginException;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.impl.ILibrary;
import com.book.renew.renovae.library.impl.fmu.FmuLibrary;
import com.book.renew.renovae.library.impl.usp.UspLibrary;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by ricardo on 03/08/16.
 */
public class LibraryManager implements Serializable {

    private ILibrary _library;
    private LoginParameters _login_params;

    private ArrayList<IBorrow> _borrows = null;

    /**
     * Map with universities
     */
    private static final Map<String, Class<? extends ILibrary>> universities =
            new HashMap<String, Class<? extends ILibrary>>() {
        {
            put("USP", UspLibrary.class);
            put("FMU", FmuLibrary.class);
        }
    };

    public static boolean universityExists(String university) {
        return universities.containsKey(university);
    }

    public static Set<String> getUniversities() {
        return universities.keySet();
    }

    public LibraryManager(LoginParameters login_params) {
        this._login_params = login_params;
    }

    public LibraryManager(LoginParameters login_params, boolean login) throws InvalidUniversityException, UnexpectedPageContent, LoginException, IOException {
        this._login_params = login_params;
        if (login)
            createLibrary();
    }

    public LoginParameters getLoginParams() {
        return _login_params;
    }

    public ArrayList<IBorrow> getBorrowedBooks() throws LogoutException, IOException, LoginException, InvalidUniversityException, UnexpectedPageContent {
        return getBorrowedBooks(false);
    }

    public ArrayList<IBorrow> getCachedBorrows() {
        return _borrows;
    }

    private void createLibrary() throws InvalidUniversityException, UnexpectedPageContent, LoginException, IOException {
        if (_library == null) {
            Class<? extends ILibrary> library_class = universities.get(_login_params.university);
            if (library_class != null) {
                try {
                    _library = library_class.newInstance();
                } catch (InstantiationException e) {
                    throw new InvalidUniversityException();
                } catch (IllegalAccessException e) {
                    throw new InvalidUniversityException();
                } catch (Exception e) {
                    //Exceptions throwable by the constructor
                    throw e;
                }
                _library.login(_login_params.username, _login_params.password);
            } else
                throw new InvalidUniversityException();
        }
    }

    private void relogin() throws UnexpectedPageContent, LoginException, IOException, InvalidUniversityException {
        try {
            _library = _library.getClass().newInstance();
        } catch (Exception e) {
            throw new InvalidUniversityException();
        }
        _library.login(_login_params.username, _login_params.password);
    }

    public ArrayList<IBorrow> getBorrowedBooks(boolean reload) throws InvalidUniversityException, UnexpectedPageContent, LoginException, IOException {
        //TODO: get books in the database and if it is empty, load again
        if (!reload && _borrows != null) {

        }
        else {
            //Not found in database or it needs to be reloaded
            createLibrary();
            try {
                _borrows = _library.getBorrowedBooks();
            } catch (LogoutException e) {
                //Logout: login again
                relogin();
                try {
                    _borrows = _library.getBorrowedBooks();
                } catch (LogoutException e1) {
                    throw new UnexpectedPageContent();
                }

            }
        }
        //TODO: save in the database
        return _borrows;
    }

    public void renew(IBorrow borrow) throws RenewException, UnexpectedPageContent, IOException, LoginException, InvalidUniversityException {
        createLibrary();
        try {
            borrow.renew();
        }
        catch (LogoutException e) {
            relogin();
            try {
                borrow.renew();
            } catch (LogoutException e1) {
                throw new UnexpectedPageContent();
            }
        }
    }
}
