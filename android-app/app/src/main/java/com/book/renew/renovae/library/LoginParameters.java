package com.book.renew.renovae.library;

import java.io.Serializable;

/**
 * Created by ricardo on 04/08/16.
 */
public class LoginParameters implements Serializable {
    public final String username;
    public final String password;
    public final String university;
    public transient final boolean save;

    public LoginParameters(String username, String password, String university, boolean save) {
        this.username = username;
        this.password = password;
        this.university = university;
        this.save = save;
    }
    public LoginParameters(String username, String password, String university) {
        this(username, password, university, false);
    }
}
