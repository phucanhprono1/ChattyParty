package com.example.chattyparty.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;



public class FriendRequestDB extends SQLiteOpenHelper {
    static final String DATABASE_NAME="FriendRequest.db";
    private Context context;

    public FriendRequestDB(Context context){
        super(context,DATABASE_NAME,null,1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(FeedEntry.SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL(FeedEntry.SQL_DELETE_ENTRIES);
        onCreate(sqLiteDatabase);
    }

    public static class FeedEntry implements BaseColumns {
        public static final String TABLE_NAME = "friendRequest";
        public static final String COLUMN_NAME_ID = "id";
        public static final String COLUMN_NAME_SENDER = "sender";
        public static final String COLUMN_NAME_RECEIVER = "receiver";
        public static final String COLUMN_NAME_SENDER_IMAGE = "senderImage";
        public static final String COLUMN_NAME_SENDER_NAME = "senderName";
        public static final String COLUMN_NAME_ID_ROOM = "idRoom";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_EMAIL = "email";
        public static final String COLUMN_NAME_AVATA = "avata";
        public static final String COLUMN_NAME_STATUS = "status";

        public static final String SQL_CREATE_ENTRIES =
                "CREATE TABLE " + TABLE_NAME + " (" +
                        _ID + " INTEGER PRIMARY KEY," +
                        COLUMN_NAME_RECEIVER + " TEXT," +
                        COLUMN_NAME_SENDER + " TEXT," +
                        COLUMN_NAME_SENDER_IMAGE + " TEXT," +
                        COLUMN_NAME_SENDER_NAME + " TEXT," +
                        COLUMN_NAME_ID_ROOM + " TEXT," +
                        COLUMN_NAME_ID + " TEXT," +
                        COLUMN_NAME_NAME + " TEXT," +
                        COLUMN_NAME_EMAIL + " TEXT," +
                        COLUMN_NAME_AVATA + " TEXT," +
                        COLUMN_NAME_STATUS + " TEXT)";

        // SQL command to delete the table
        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}