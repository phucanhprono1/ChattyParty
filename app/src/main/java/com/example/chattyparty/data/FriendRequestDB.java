package com.example.chattyparty.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

import com.example.chattyparty.model.FriendRequest;

import java.util.ArrayList;
import java.util.List;


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
    public void addFriendRequest(FriendRequest friendRequest){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("INSERT INTO "+FeedEntry.TABLE_NAME+" VALUES('"+friendRequest.sender+"','"+friendRequest.getReceiver()+"','"+friendRequest.id+"','"+friendRequest.senderImage+"','"+friendRequest.senderName+"','"+friendRequest.idRoom+"','"+friendRequest.name+"','"+friendRequest.email+"','"+friendRequest.avata+"','"+friendRequest.status+"')");
        db.close();
    }
    public void deleteFriendRequest(String id){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("DELETE FROM "+FeedEntry.TABLE_NAME+" WHERE "+FeedEntry.COLUMN_NAME_ID+"='"+id+"'");
        db.close();
    }
    public void updateFriendRequest(String id,String status){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL("UPDATE "+FeedEntry.TABLE_NAME+" SET "+FeedEntry.COLUMN_NAME_STATUS+"='"+status+"' WHERE "+FeedEntry.COLUMN_NAME_ID+"='"+id+"'");
        db.close();
    }

    public List<FriendRequest> getAllFriendRequest(){
        List<FriendRequest> listFriendRequest=new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor cursor=db.rawQuery("SELECT * FROM "+FeedEntry.TABLE_NAME,null);
        if(cursor.moveToFirst()){
            do{
                FriendRequest friendRequest=new FriendRequest();
                friendRequest.sender=cursor.getString(0);
                friendRequest.receiver=cursor.getString(1);
                friendRequest.id=cursor.getString(2);
                friendRequest.senderImage=cursor.getString(3);
                friendRequest.senderName=cursor.getString(4);
                friendRequest.idRoom=cursor.getString(5);
                friendRequest.name=cursor.getString(6);
                friendRequest.email=cursor.getString(7);
                friendRequest.avata=cursor.getString(8);
                friendRequest.status=cursor.getString(9);
                listFriendRequest.add(friendRequest);
            }while(cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return listFriendRequest;
    }
    public void dropDB(){
        SQLiteDatabase db=this.getWritableDatabase();
        db.execSQL(FeedEntry.SQL_DELETE_ENTRIES);
        db.close();
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
                        COLUMN_NAME_SENDER + " TEXT PRIMARY KEY," +
                        COLUMN_NAME_RECEIVER + " TEXT," +
                        COLUMN_NAME_ID + " TEXT," +
                        COLUMN_NAME_SENDER_IMAGE + " TEXT," +
                        COLUMN_NAME_SENDER_NAME + " TEXT," +
                        COLUMN_NAME_ID_ROOM + " TEXT," +
                        COLUMN_NAME_NAME + " TEXT," +
                        COLUMN_NAME_EMAIL + " TEXT," +
                        COLUMN_NAME_AVATA + " TEXT," +
                        COLUMN_NAME_STATUS + " TEXT)";

        // SQL command to delete the table
        public static final String SQL_DELETE_ENTRIES =
                "DROP TABLE IF EXISTS " + TABLE_NAME;
    }
}
