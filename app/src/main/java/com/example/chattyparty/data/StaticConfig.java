package com.example.chattyparty.data;


import com.example.chattyparty.model.Friend;
import com.example.chattyparty.model.FriendRequest;

import java.lang.reflect.Array;
import java.util.ArrayList;

public class StaticConfig {
    public static int REQUEST_CODE_REGISTER = 2000;
    public static String STR_EXTRA_ACTION_LOGIN = "login";
    public static String STR_EXTRA_ACTION_RESET = "resetpass";
    public static String STR_EXTRA_ACTION = "action";
    public static String STR_EXTRA_USERNAME = "username";
    public static String STR_EXTRA_PASSWORD = "password";
    public static String STR_DEFAULT_URI = "default";
    public static String UID = "";
    public static String EMAIL = "";
    public static String NAME = "";
    public static String AVATA = "";
    public static String CITY = "";
    public static String COUNTRY = "";
    public static String PROFESSION = "";
    public static String BIO = "";

    //TODO only use this UID for debug mode
//    public static String UID = "6kU0SbJPF5QJKZTfvW1BqKolrx22";
    public static String INTENT_KEY_CHAT_FRIEND = "friendname";
    public static String INTENT_KEY_CHAT_AVATA = "friendavata";
    public static String INTENT_KEY_CHAT_ID = "friendid";
    public static String INTENT_KEY_CHAT_ROOM_ID = "roomid";
    public static long TIME_TO_REFRESH = 1 * 1000;
    public static long TIME_TO_OFFLINE = 2 * 1000;
    public static String ID_FRIEND_REQ = null;
    public static Friend FRIEND_REQUEST = null;
    public static ArrayList<String> LIST_FRIEND_ID = new ArrayList<>();
    public static ArrayList<FriendRequest> LIST_FRIEND_REQUEST = new ArrayList<>();
}
