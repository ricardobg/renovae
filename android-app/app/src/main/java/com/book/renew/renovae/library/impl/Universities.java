package com.book.renew.renovae.library.impl;

import com.book.renew.renovae.library.ILibrary;
import com.book.renew.renovae.library.impl.fmu.FmuLibrary;
import com.book.renew.renovae.library.impl.usp.UspLibrary;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ricardo on 02/08/16.
 */
public class Universities {
    private static Universities instance = null;

    private Map<String, Class<? extends ILibrary>> universities;

    public static Universities instance() {
        if (instance == null)
            instance = new Universities();
        return instance;
    }

    public Map<String, Class<? extends ILibrary>> get() {
        return universities;
    }

    private Universities() {
        //Populate universities
        universities = new HashMap<>();
        universities.put("USP", UspLibrary.class);
        universities.put("FMU", FmuLibrary.class);
    }
}
