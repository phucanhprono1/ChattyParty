package com.example.chattyparty.model;

public class FriendRequest {
    private String receiver;
    private String sender;
    private String senderImage;
    private String senderName;



    private String status;

    public String getIdRoom() {
        return idRoom;
    }

    public void setIdRoom(String idRoom) {
        this.idRoom = idRoom;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAvata() {
        return avata;
    }

    public void setAvata(String avata) {
        this.avata = avata;
    }

    private String idRoom;
    private String id;

//    public String getIdSender() {
//        return idSender;
//    }
//
//    public void setIdSender(String idSender) {
//        this.idSender = idSender;
//    }
//
//    public String getIdReceiver() {
//        return idReceiver;
//    }
//
//    public void setIdReceiver(String idReceiver) {
//        this.idReceiver = idReceiver;
//    }
//
//
//    public String getTxt() {
//        return txt;
//    }
//
//    public void setTxt(String txt) {
//        this.txt = txt;
//    }
//
//    public long getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(long timestamp) {
//        this.timestamp = timestamp;
//    }

    private String name, email, avata;
//    private String idSender,idReceiver,txt,onlStatus;
//    private long timestamp;

//    public String getOnlStatus() {
//        return onlStatus;
//    }
//
//    public void setOnlStatus(String onlStatus) {
//        this.onlStatus = onlStatus;
//    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSenderImage() {
        return senderImage;
    }

    public void setSenderImage(String senderImage) {
        this.senderImage = senderImage;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public FriendRequest() {
    }

    public String getStatus() {
        return status;
    }

    public FriendRequest(String receiver, String sender, String senderImage, String senderName, String status) {
        this.receiver = receiver;
        this.sender = sender;
        this.senderImage = senderImage;
        this.senderName = senderName;
        this.status = status;

    }

    public void setStatus(String status) {
        this.status = status;
    }
}