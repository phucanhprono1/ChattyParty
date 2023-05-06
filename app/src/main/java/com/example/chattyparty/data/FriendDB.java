package com.example.chattyparty.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import android.widget.Toast;

import com.example.chattyparty.model.Friend;
import com.example.chattyparty.model.ListFriend;


public final class FriendDB extends SQLiteOpenHelper{
    static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "FriendChat.db";
    //private static FriendDBHelper mDbHelper = null;
    private Context context;

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    public FriendDB(Context context) {
        super(context, DATABASE_NAME,null,1);
        Log.d("DB Manager","DB Manager");

        this.context=context;
    }

    public boolean checkFriendExist(String friendId) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(
                FeedEntry.TABLE_NAME,   // The table to query
                new String[]{FeedEntry.COLUMN_NAME_ID},   // The columns to return
                FeedEntry.COLUMN_NAME_ID + "=?",          // The columns for the WHERE clause
                new String[]{friendId},                   // The values for the WHERE clause
                null,                                     // Don't group the rows
                null,                                     // Don't filter by row groups
                null                                      // The sort order
        );

        boolean exist = cursor.moveToFirst();
        cursor.close();
        return exist;
    }

    public void addFriend(Friend friend) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put(FeedEntry.COLUMN_NAME_ID, friend.id);
        values.put(FeedEntry.COLUMN_NAME_NAME, friend.name);
        values.put(FeedEntry.COLUMN_NAME_EMAIL, friend.email);
        values.put(FeedEntry.COLUMN_NAME_ID_ROOM, friend.idRoom);
        values.put(FeedEntry.COLUMN_NAME_AVATA, friend.avata);
        // Insert the new row, returning the primary key value of the new row
        db.insert(FeedEntry.TABLE_NAME,null, values);
        db.close();
    }


    public void addListFriend(ListFriend listFriend){
        for(Friend friend: listFriend.getListFriend()){
            addFriend(friend);
        }
    }
    public void deleteFriend(String friendId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(FeedEntry.TABLE_NAME, FeedEntry.COLUMN_NAME_ID + "=?", new String[]{friendId});
        db.close();
    }
    public ListFriend getListFriend() {
        ListFriend listFriend = new ListFriend();
        SQLiteDatabase db = this.getReadableDatabase();
        // Define a projection that specifies which columns from the database
// you will actually use after this query.
        try {
            Cursor cursor = db.rawQuery("select * from " + FeedEntry.TABLE_NAME, null);
            while (cursor.moveToNext()) {
                Friend friend = new Friend();
                friend.id = cursor.getString(0);
                friend.name = cursor.getString(1);
                friend.email = cursor.getString(2);
                friend.idRoom = cursor.getString(3);
                friend.avata = cursor.getString(4);
                listFriend.getListFriend().add(friend);
            }
            cursor.close();
        }catch (Exception e){
            return new ListFriend();
        }
        return listFriend;
    }

    public void dropDB(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SQL_DELETE_ENTRIES);
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(SQL_CREATE_ENTRIES);
        Toast.makeText(context, "Create Database successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
        Toast.makeText(context, "Drop successfully", Toast.LENGTH_SHORT).show();
    }

    /* Inner class that defines the table contents */
    public static class FeedEntry implements BaseColumns {
        static final String TABLE_NAME = "friend";
        static final String COLUMN_NAME_ID = "friendID";
        static final String COLUMN_NAME_NAME = "name";
        static final String COLUMN_NAME_EMAIL = "email";
        static final String COLUMN_NAME_ID_ROOM = "idRoom";
        static final String COLUMN_NAME_AVATA = "avata";
    }

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + FeedEntry.TABLE_NAME + " (" +
                    FeedEntry.COLUMN_NAME_ID + " TEXT PRIMARY KEY," +
                    FeedEntry.COLUMN_NAME_NAME + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_EMAIL + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_ID_ROOM + TEXT_TYPE + COMMA_SEP +
                    FeedEntry.COLUMN_NAME_AVATA + TEXT_TYPE + " )";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + FeedEntry.TABLE_NAME;

}
