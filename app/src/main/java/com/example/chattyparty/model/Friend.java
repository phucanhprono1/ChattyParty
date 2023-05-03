package com.example.chattyparty.model;



public class Friend extends User{
    public String id;
    public String idRoom;

    public Friend() {
    }

    public Friend(String id, String name, String email, String avt, String idRoom) {
        super(id, name, email, avt);
        this.idRoom = idRoom;
    }
}
