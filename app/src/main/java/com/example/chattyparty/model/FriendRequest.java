package com.example.chattyparty.model;

public class FriendRequest {
    private String id;
    private int profileImage;
    private String name;
    private String status;

    public FriendRequest(int profileImage, String name, String status) {
        this.profileImage = profileImage;
        this.name = name;
        this.status = status;
    }

    public int getProfileImage() {
        return profileImage;
    }

    public String getName() {
        return name;
    }

    public String getStatus() {
        return status;
    }
}