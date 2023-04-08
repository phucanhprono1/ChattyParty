package com.example.chattyparty.model;

import android.net.Uri;

import java.io.Serializable;

public class User implements Serializable {
    public String userId;
    public String username;
    public String email;
    public Uri avt;

    public Uri getAvt() {
        return avt;
    }

    public void setAvt(Uri avt) {
        this.avt = avt;
    }

    public User() {
        // Default constructor required for calls to DataSnapshot.getValue(User.class)
    }

    public User(String userId, String username, String email) {
        this.userId = userId;
        this.username = username;
        this.email = email;
    }
}