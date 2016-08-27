package com.book.renew.renovae.android;

import android.os.Bundle;
import android.util.Log;

import com.book.renew.renovae.library.exception.DefaultMessageException;
import com.book.renew.renovae.library.exception.InvalidUniversityException;
import com.book.renew.renovae.library.exception.LoginException;
import com.book.renew.renovae.library.exception.LogoutException;
import com.book.renew.renovae.library.exception.network.LibraryUnavailableException;
import com.book.renew.renovae.library.exception.network.NoInternetException;
import com.book.renew.renovae.library.exception.renew.RenewException;
import com.book.renew.renovae.library.exception.UnexpectedPageContentException;
import com.book.renew.renovae.library.exception.network.NetworkException;
import com.book.renew.renovae.library.impl.IBorrow;
import com.book.renew.renovae.library.impl.ILibrary;
import com.book.renew.renovae.library.impl.fmu.FmuLibrary;
import com.book.renew.renovae.library.impl.test.TestLibrary;
import com.book.renew.renovae.library.impl.unesp.UnespLibrary;
import com.book.renew.renovae.library.impl.usp.UspLibrary;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Class to manage a Library
 */
public final class LibraryManager implements Serializable {

    private static final String EXTRA_LIBRARY_MANAGER =
            "com.book.renew.renovae.library_manager";
    private static LibraryManager _manager = null;

    //Library being managed
    private ILibrary _library;
    //Login parameters used to login
    private LoginParameters _login_params;

    private ArrayList<Borrow> _borrows = null;

    /**
     * Map with universities
     */
    private static final Map<String, Class<? extends ILibrary>> universities =
            new HashMap<String, Class<? extends ILibrary>>() {
        {
            put("USP", UspLibrary.class);
            put("FMU", FmuLibrary.class);
            put("UNESP", UnespLibrary.class);
            put("TEST", TestLibrary.class);
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

    public ArrayList<Borrow> getCachedBorrows() {
        return _borrows;
    }

    public ArrayList<Borrow> getBorrowedBooks() throws LogoutException, NetworkException, LoginException, InvalidUniversityException, UnexpectedPageContentException {
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

    private void relogin() throws UnexpectedPageContentException, LoginException, NetworkException, InvalidUniversityException {
        try {
            _library = _library.getClass().newInstance();
        } catch (Exception e) {
            throw new InvalidUniversityException();
        }
        _library.login(_login_params.username, _login_params.password);
    }

    public ArrayList<Borrow> getBorrowedBooks(boolean reload) throws InvalidUniversityException, UnexpectedPageContentException, LoginException, NetworkException {

        //TODO: get books in the database and if it is empty, load again
        ArrayList<IBorrow> tempBorrows = new ArrayList<>();
        if (!reload && _borrows != null) {

        }
        else {
            //Not found in database or it needs to be reloaded
            createLibrary();
            try {
                tempBorrows = _library.loadBorrowsList();
            } catch (LogoutException e) {
                //Logout: login again
                System.out.println("[LIBRARY MANAGER] LOGOUT");
                relogin();
                try {
                    tempBorrows = _library.loadBorrowsList();
                } catch (LogoutException e1) {
                    throw new UnexpectedPageContentException();
                }
            }

            //Now, loads each borrow info
            ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

            ArrayList<Callable<Object>> executeList = new ArrayList<>(tempBorrows.size());
            for (final IBorrow b : tempBorrows) {
                executeList.add(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        b.load(_library);
                        return null;
                    }
                });
            }
            try {
                for (Future<Object> future : executor.invokeAll(executeList)) {
                    future.get();
                }
            } catch (InterruptedException e) {
                throw new UnexpectedPageContentException(e.getMessage());
            } catch (ExecutionException e) {
                try {
                    throw e.getCause();
                } catch (InvalidUniversityException e1) {
                    throw e1;
                }
                catch (NetworkException e1) {
                    throw e1;
                }
                catch (UnexpectedPageContentException e1) {
                    throw e1;
                }
                catch (LoginException e1) {
                    throw e1;
                } catch (Throwable throwable) {
                    throw new UnexpectedPageContentException(e.getMessage());
                }
            }
        }

        //TODO: save in the database

        //Copy to Borrow List
        _borrows = new ArrayList<>(tempBorrows.size());
        for (IBorrow b : tempBorrows)
            _borrows.add(new Borrow(b));
        return _borrows;
    }

    public void renew(Borrow borrow) throws RenewException, UnexpectedPageContentException, NetworkException, LoginException, InvalidUniversityException {
        createLibrary();
        try {
            borrow.getBorrow().renew(_library);
        }
        catch (LogoutException e) {
            relogin();
            try {
                borrow.getBorrow().renew(_library);
            } catch (LogoutException e1) {
                throw new UnexpectedPageContentException();
            }
        }
    }
}
