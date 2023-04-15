package com.example.chattyparty.model;



public class User {
    public String name;
    public String email;
    public String avata;
    public String id;

    public Message message;
    public Status status;


    public User(String id, String name, String email, String avt) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.avata = avt;
    }

    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }
    public User(){
        status = new Status();
        message = new Message();
        status.isOnline = false;
        status.timestamp = 0;
        message.idReceiver = "0";
        message.idSender = "0";
        message.text = "";
        message.timestamp = 0;
    }
}
