package com.example.chattyparty.service;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Binder;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.provider.Settings;

import android.util.Base64;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.example.chattyparty.ChatActivity;
import com.example.chattyparty.MainActivity;
import com.example.chattyparty.R;
import com.example.chattyparty.data.FriendDB;
import com.example.chattyparty.data.GroupDB;
import com.example.chattyparty.data.StaticConfig;
import com.example.chattyparty.model.Friend;
import com.example.chattyparty.model.Group;
import com.example.chattyparty.model.ListFriend;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;



public class FriendChatService extends Service {
    private static String TAG = "FriendChatService";
    // Binder given to clients
    public final IBinder mBinder = new LocalBinder();
    public Map<String, Boolean> mapMark;
    public Map<String, Query> mapQuery;
    public Map<String, ChildEventListener> mapChildEventListenerMap;
    public Map<String, String> mapBitmap;
    public ArrayList<String> listKey;
    public ListFriend listFriend;
    public ArrayList<Group> listGroup;
    public CountDownTimer updateOnline;
    public FriendDB friendDB;
    public GroupDB groupDB;
    public FriendChatService() {
    }


    @Override
    public void onCreate() {
        friendDB = new FriendDB(getBaseContext());
        groupDB = new GroupDB(getBaseContext());
        super.onCreate();
        mapMark = new HashMap<>();
        mapQuery = new HashMap<>();
        mapChildEventListenerMap = new HashMap<>();
        listFriend = friendDB.getListFriend();
        listGroup = groupDB.getListGroups();
        listKey = new ArrayList<>();
        mapBitmap = new HashMap<>();
        updateOnline = new CountDownTimer(System.currentTimeMillis(), StaticConfig.TIME_TO_REFRESH) {
            @Override
            public void onTick(long l) {
                ServiceUtils.updateUserStatus(getApplicationContext());
            }

            @Override
            public void onFinish() {

            }
        };
        updateOnline.start();

        if (listFriend.getListFriend().size() > 0 || listGroup.size() > 0) {
            //Dang ky lang nghe cac room tai day
            for (final Friend friend : listFriend.getListFriend()) {
                if (!listKey.contains(friend.idRoom)) {
                    mapQuery.put(friend.idRoom, FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("message/" + friend.idRoom).limitToLast(1));
                    mapChildEventListenerMap.put(friend.idRoom, new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if (mapMark.get(friend.idRoom) != null && mapMark.get(friend.idRoom)) {

                                if (mapBitmap.get(friend.idRoom) == null) {
                                    if (!friend.avata.equals(StaticConfig.STR_DEFAULT_URI)) {
                                        mapBitmap.put(friend.idRoom, friend.avata);
                                    } else {
                                        mapBitmap.put(friend.idRoom, StaticConfig.STR_DEFAULT_URI);
                                    }
                                }

                                createNotify(friend.name, (String) ((HashMap) dataSnapshot.getValue()).get("text"), friend.idRoom.hashCode(), mapBitmap.get(friend.idRoom), false);

                            } else {
                                mapMark.put(friend.idRoom, true);
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    listKey.add(friend.idRoom);
                }
                mapQuery.get(friend.idRoom).addChildEventListener(mapChildEventListenerMap.get(friend.idRoom));
            }

            for (final Group group : listGroup) {
                if (!listKey.contains(group.id)) {
                    mapQuery.put(group.id, FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("message/" + group.id).limitToLast(1));
                    mapChildEventListenerMap.put(group.id, new ChildEventListener() {
                        @Override
                        public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                            if (mapMark.get(group.id) != null && mapMark.get(group.id)) {
                                if (mapBitmap.get(group.id) == null) {
                                    mapBitmap.put(group.id, "https://firebasestorage.googleapis.com/v0/b/chattyparty-7d883.appspot.com/o/ic_notify_group.png?alt=media&token=5248ec61-a1f0-4f3f-9091-4b10dd830f96");
                                }
                                createNotify(group.groupInfo.get("name"), (String) ((HashMap) dataSnapshot.getValue()).get("text"), group.id.hashCode(), mapBitmap.get(group.id) , true);
                            } else {
                                mapMark.put(group.id, true);
                            }
                        }

                        @Override
                        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onChildRemoved(DataSnapshot dataSnapshot) {

                        }

                        @Override
                        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    listKey.add(group.id);
                }
                mapQuery.get(group.id).addChildEventListener(mapChildEventListenerMap.get(group.id));
            }

        } else {
            stopSelf();
        }
    }

    public void stopNotify(String id) {
        mapMark.put(id, false);
    }

    public void createNotify(String name, String content, int id, String icon, boolean isGroup) {
        if (Build.VERSION.SDK_INT >= 24) {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{Manifest.permission.POST_NOTIFICATIONS}, 101);
            } else {
                Intent activityIntent = new Intent(this, MainActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, activityIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_ONE_SHOT);
                NotificationCompat.Builder notificationBuilder = new
                        NotificationCompat.Builder(this)
                        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_notify_group))
                        .setContentTitle(name)
                        .setContentText(content)
                        .setContentIntent(pendingIntent)
                        .setVibrate(new long[] { 1000, 1000})
                        .setSound(Settings.System.DEFAULT_NOTIFICATION_URI)
                        .setAutoCancel(true);
                if (isGroup) {
                    notificationBuilder.setSmallIcon(R.drawable.ic_tab_group);
                } else {
                    notificationBuilder.setSmallIcon(R.drawable.ic_tab_person);
                }
                NotificationManager notificationManager =
                        (NotificationManager) this.getSystemService(
                                Context.NOTIFICATION_SERVICE);
                notificationManager.cancel(id);
                notificationManager.notify(id,
                        notificationBuilder.build());
            }
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "OnStartService");
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "OnBindService");
        return mBinder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        for (String id : listKey) {
            mapQuery.get(id).removeEventListener(mapChildEventListenerMap.get(id));
        }
        mapQuery.clear();
        mapChildEventListenerMap.clear();
        mapBitmap.clear();
        updateOnline.cancel();
        Log.d(TAG, "OnDestroyService");
    }

    public class LocalBinder extends Binder {
        public FriendChatService getService() {
            // Return this instance of LocalService so clients can call public methods
            return FriendChatService.this;
        }
    }
}
