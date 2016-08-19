package com.book.renew.renovae.library;

import android.os.Bundle;

import com.book.renew.renovae.library.exception.InvalidUniversityException;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.RenewException;
import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.impl.ILibrary;
import com.book.renew.renovae.library.impl.fmu.FmuLibrary;
import com.book.renew.renovae.library.impl.usp.UspLibrary;
import com.book.renew.renovae.util.Util;

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

    private static final String EXTRA_LIBRARY_MANAGER =
            "com.book.renew.renovae.library_manager";
    private static LibraryManager _manager = null;


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


    /**
     * Create new Library Manager
     * @param login_params
     */
    public static void create(LoginParameters login_params) {
        _manager = new LibraryManager(login_params);
    }

    /**
     * * Create new Library Manager and login
     * @param login_params
     * @throws InvalidUniversityException
     * @throws UnexpectedPageContentException
     * @throws LoginException
     * @throws NetworkException
     */
    public static void createAndLogin(LoginParameters login_params)
            throws InvalidUniversityException, UnexpectedPageContentException, LoginException, NetworkException {
        _manager = new LibraryManager(login_params, true);
    }

    /**
     * Checks if Library is loaded. Try to load from savedInstance if it is not and returns if it could load
     * @param savedInstance
     * @return
     */
    public static boolean loaded(Bundle savedInstance) {
        //Already loaded
        if (_manager != null)
            return  true;
        if (savedInstance == null)
            return false;
        LibraryManager tempLibrary = (LibraryManager) savedInstance.getSerializable(EXTRA_LIBRARY_MANAGER);
        if (tempLibrary == null)
            return false;
        _manager = tempLibrary;
        return true;
    }

    /**
     * Save instance of library manager in Bundle
     * @param saveInstance
     */
    public static void saveInstance(Bundle saveInstance) {
        saveInstance.putSerializable(EXTRA_LIBRARY_MANAGER, _manager);
    }

    /**
     * Get the LibraryManager
     * @return
     */
    public static LibraryManager get() {
        return _manager;
    }

    public LoginParameters getLoginParams() {
        return _login_params;
    }

    public ArrayList<IBorrow> getCachedBorrows() {
        return _borrows;
    }

    public ArrayList<IBorrow> getBorrowedBooks() throws LogoutException, NetworkException, LoginException, InvalidUniversityException, UnexpectedPageContentException {
        return getBorrowedBooks(false);
    }

    private LibraryManager(LoginParameters login_params) {
        this._login_params = login_params;

    }

    private LibraryManager(LoginParameters login_params, boolean login)
            throws InvalidUniversityException, UnexpectedPageContentException, LoginException, NetworkException {
        this._login_params = login_params;
        if (login)
            createLibrary();
    }

    private void createLibrary() throws InvalidUniversityException, UnexpectedPageContentException, LoginException, NetworkException {
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

    private void relogin() throws UnexpectedPageContentException, LoginException, NetworkException, InvalidUniversityException {
        try {
            _library = _library.getClass().newInstance();
        } catch (Exception e) {
            throw new InvalidUniversityException();
        }
        _library.login(_login_params.username, _login_params.password);
    }

    public ArrayList<IBorrow> getBorrowedBooks(boolean reload) throws InvalidUniversityException, UnexpectedPageContentException, LoginException, NetworkException {
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
                    throw new UnexpectedPageContentException();
                }

            }
        }
        //TODO: save in the database
        return _borrows;
    }

    public void renew(IBorrow borrow) throws RenewException, UnexpectedPageContentException, NetworkException, LoginException, InvalidUniversityException {
        createLibrary();
        try {
            borrow.renew();
        }
        catch (LogoutException e) {
            relogin();
            try {
                borrow.renew();
            } catch (LogoutException e1) {
                throw new UnexpectedPageContentException();
            }
        }
    }
}
