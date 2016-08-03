package com.book.renew.renovae.library.impl;

import com.book.renew.renovae.library.IBorrow;
import com.book.renew.renovae.library.ILibrary;
import com.book.renew.renovae.library.impl.fmu.FmuLibrary;
import com.book.renew.renovae.library.impl.usp.UspLibrary;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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

    public Set<String> getUniversities() {
        return universities.keySet();
    }

    public boolean hasUniversity(String university) {
        return universities.containsKey(university);
    }

    public ILibrary getUniversity(String university) {
        try {
            return universities.get(university).newInstance();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }
    }

    private Universities() {
        //Populate universities
        universities = new HashMap<>();
        universities.put("USP", UspLibrary.class);
        universities.put("FMU", FmuLibrary.class);
    }
}
