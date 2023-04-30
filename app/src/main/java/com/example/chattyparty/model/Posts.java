package com.example.chattyparty.model;

public class Posts {
    private String datePost,postDesc,postImageUrl,userProfileImageUrl,username;

    public Posts() {
    }

    public Posts(String datePost, String postDesc, String postImageUrl, String userProfileImageUrl, String username) {
        this.datePost = datePost;
        this.postDesc = postDesc;
        this.postImageUrl = postImageUrl;
        this.userProfileImageUrl = userProfileImageUrl;
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
        return postImageUrl;
    }

    public void setPostImageURL(String postImageUrl) {
        this.postImageUrl = postImageUrl;
    }

    public String getUserProfileImageURL() {
        return userProfileImageUrl;
    }

    public void setUserProfileImageURL(String userProfileImageURL) {
        this.userProfileImageUrl = userProfileImageURL;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}