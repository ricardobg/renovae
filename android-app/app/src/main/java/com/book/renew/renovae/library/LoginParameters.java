package com.book.renew.renovae.library;

import java.io.Serializable;

/**
 * Created by ricardo on 04/08/16.
 */
public class LoginParameters implements Serializable {
    public final String username;
    public final String password;
    public final String university;

    public LoginParameters(String username, String password, String university) {
        this.username = username;
        this.password = password;
        this.university = university;
    }

}
