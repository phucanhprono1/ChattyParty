package com.example.chattyparty.model;

public class Posts {
    private String datePost,postDesc,postImageURL,userProfileImageURL,username;

    public Posts() {
    }

    public Posts(String datePost, String postDesc, String postImageURL, String userProfileImageURL, String username) {
        this.datePost = datePost;
        this.postDesc = postDesc;
        this.postImageURL = postImageURL;
        this.userProfileImageURL = userProfileImageURL;
        this.username = username;
    }

    public String getDatePost() {
        return datePost;
    }

    public void setDatePost(String datePost) {
        this.datePost = datePost;
    }

    public String getPostDesc() {
        return postDesc;
    }

    public void setPostDesc(String postDesc) {
        this.postDesc = postDesc;
    }

    public String getPostImageURL() {
        return postImageURL;
    }

    public void setPostImageURL(String postImageURL) {
        this.postImageURL = postImageURL;
    }

    public String getUserProfileImageURL() {
        return userProfileImageURL;
    }

    public void setUserProfileImageURL(String userProfileImageURL) {
        this.userProfileImageURL = userProfileImageURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}