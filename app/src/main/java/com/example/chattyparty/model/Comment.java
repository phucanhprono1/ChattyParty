package com.example.chattyparty.model;

public class Comment{
    private String username,profileImageURL,comment;

    public Comment() {
    }

    public Comment(String username, String profileImageURL, String comment) {
        this.username = username;
        this.profileImageURL = profileImageURL;
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getProfileImageURL() {
        return profileImageURL;
    }

    public void setProfileImageURL(String profileImageURL) {
        this.profileImageURL = profileImageURL;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
