package com.example.chattyparty;

import static android.content.ContentValues.TAG;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chattyparty.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.chattyparty.data.StaticConfig;
import com.example.chattyparty.model.Conversation;
import com.example.chattyparty.model.Message;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import android.Manifest;

import org.json.JSONObject;


public class ChatActivity extends AppCompatActivity implements View.OnClickListener {
    public static final String TAG = "ChatActivity";
    private RecyclerView recyclerChat;
    public static final int VIEW_TYPE_USER_MESSAGE = 0;
    public static final int VIEW_TYPE_FRIEND_MESSAGE = 1;
    private ListMessageAdapter adapter;
    private String roomId;
    private ArrayList<CharSequence> idFriend;
    private Conversation conversation;
    private ImageView btnSend;
    private EditText editWriteMessage;
    private LinearLayoutManager linearLayoutManager;
    public static HashMap<String, String> bitmapAvataFriend;
    public String bitmapAvataUser;

    public SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intentData = getIntent();
        Log.d(TAG, "onCreate: " + StaticConfig.AVATA);
        idFriend = intentData.getCharSequenceArrayListExtra(StaticConfig.INTENT_KEY_CHAT_ID);
        Log.d(TAG, "onCreate: " + idFriend);
        roomId = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_ROOM_ID);
        String nameFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_FRIEND);
        Log.d(TAG, "onCreate: " + nameFriend);
        String avataFriend = intentData.getStringExtra(StaticConfig.INTENT_KEY_CHAT_AVATA);
        FirebaseMessaging.getInstance().subscribeToTopic("all");
//        String deviceToken = FirebaseInstanceId.getInstance().getToken();
        conversation = new Conversation();
        btnSend = (ImageView) findViewById(R.id.btnSend);
        btnSend.setOnClickListener(this);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        String avtUser = StaticConfig.AVATA;
        if (!avtUser.equals(StaticConfig.AVATA)) {

            bitmapAvataUser = avtUser;
        } else {
            bitmapAvataUser = StaticConfig.AVATA;
        }

        editWriteMessage = (EditText) findViewById(R.id.editWriteMessage);
        if (idFriend != null && nameFriend != null) {
//            getSupportActionBar().setTitle(nameFriend);
            linearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
            recyclerChat = findViewById(R.id.recyclerChat);
            recyclerChat.setLayoutManager(linearLayoutManager);
            adapter = new ListMessageAdapter(this, conversation, bitmapAvataFriend, bitmapAvataUser);

            FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("message/" + roomId).addChildEventListener(new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    if (dataSnapshot.getValue() != null) {
                        HashMap mapMessage = (HashMap) dataSnapshot.getValue();
                        Message newMessage = new Message();
                        newMessage.idSender = (String) mapMessage.get("idSender");
                        newMessage.nameSender = (String) mapMessage.get("nameSender");
                        newMessage.idReceiver = (String) mapMessage.get("idReceiver");

                        newMessage.text = (String) mapMessage.get("text");
                        newMessage.timestamp = (long) mapMessage.get("timestamp");
                        conversation.getListMessageData().add(newMessage);
                        adapter.notifyDataSetChanged();
                        linearLayoutManager.scrollToPosition(conversation.getListMessageData().size() - 1);
                        // Send a push notification to the user
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        if (Build.VERSION.SDK_INT >= 24){
                            if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                                ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS},101);
                            }
                            else {
//                                if (!newMessage.idSender.equals(currentUserId)) {
//                                    // Create the notification payload
//                                    NotificationCompat.Builder builder = new NotificationCompat.Builder(getApplicationContext(), "default");
//                                    builder.setSmallIcon(R.drawable.ic_notification);
//                                    builder.setContentTitle(newMessage.nameSender);
//                                    builder.setContentText(newMessage.text);
//                                    builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
//                                    builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
//
//                                    // Create the notification channel (required for Android Oreo and above)
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                                        NotificationChannel channel = new NotificationChannel("default", "Default", NotificationManager.IMPORTANCE_DEFAULT);
//                                        NotificationManager notificationManager = getSystemService(NotificationManager.class);
//                                        notificationManager.createNotificationChannel(channel);
//                                    }
//
//                                    // Send the notification
//                                    NotificationManagerCompat notificationManager = NotificationManagerCompat.from(getApplicationContext());
//                                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//                                        // TODO: Consider calling
//                                        //    ActivityCompat#requestPermissions
//                                        // here to request the missing permissions, and then overriding
//                                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                        //                                          int[] grantResults)
//                                        // to handle the case where the user grants the permission. See the documentation
//                                        // for ActivityCompat#requestPermissions for more details.
//                                        return;
//                                    }
//                                    notificationManager.notify(0, builder.build());
//
//                                }
                            }
                        }

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
            recyclerChat.setAdapter(adapter);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            Intent result = new Intent();
            result.putExtra("idFriend", idFriend.get(0));
            setResult(RESULT_OK, result);
            this.finish();
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        Intent result = new Intent();
        result.putExtra("idFriend", idFriend.get(0));
        setResult(RESULT_OK, result);
        this.finish();
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btnSend) {
            String content = editWriteMessage.getText().toString().trim();
            if (content.length() > 0) {
                editWriteMessage.setText("");
                Message newMessage = new Message();
                newMessage.text = content;
                newMessage.idSender = StaticConfig.UID;
                newMessage.nameSender = StaticConfig.NAME;
                newMessage.idReceiver = roomId;
                newMessage.timestamp = System.currentTimeMillis();
                FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("message/" + roomId).push().setValue(newMessage);
                if (Build.VERSION.SDK_INT >= 24){
                    if (ContextCompat.checkSelfPermission(ChatActivity.this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(ChatActivity.this, new String[]{Manifest.permission.POST_NOTIFICATIONS},101);
                    }
                    else {
                        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        for( CharSequence id : idFriend) {
                            String receiverUserId = id.toString();
                            Log.d("FCM", receiverUserId);// Lấy ID người gửi tin nhắn
                            String notificationMessage = newMessage.text; // Nội dung tin nhắn
                            // Gọi phương thức để gửi thông báo đẩy
                            sendPushNotification(receiverUserId, notificationMessage);
                        }
                    }
                }
            }
        }
    }
    private void sendPushNotification(String receiverUserId, String notificationMessage) {
        Log.d("FCM", "Preparing to send notification with token: " + receiverUserId+" "+notificationMessage);
        Log.d("FCM", "Preparing to send notification");
        // Lấy FCM token của người nhận tin nhắn
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/")
                .getReference("users").child(receiverUserId);
        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    User receiverUser = dataSnapshot.getValue(User.class);
                    String fcmToken = receiverUser.getFcmToken();

                    // Gửi thông báo đẩy bằng FCM
                    if (fcmToken != null && !fcmToken.isEmpty()) {
                        Log.d("FCM", "Sending notification"+fcmToken);
                        sendNotificationUsingFCM(fcmToken, notificationMessage);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle error
            }
        });
    }

    private void sendNotificationUsingFCM(String fcmToken, String notificationMessage) {
        // Gửi thông báo đẩy bằng FCM
        Log.d("FCM", "Preparing to send notification with token: " + fcmToken+" "+notificationMessage);
        try {
//            JSONObject notification = new JSONObject();
//            notification.put("title", StaticConfig.NAME); // Tiêu đề thông báo
//            notification.put("body", notificationMessage); // Nội dung thông báo
//
//            JSONObject message = new JSONObject();
//            message.put("token", fcmToken); // FCM token của người nhận
//            message.put("notification", notification);
//            JSONObject sending = new JSONObject();
//            sending.put("message", message);
            JSONObject jsonObject  = new JSONObject();

            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title",StaticConfig.NAME);
            notificationObj.put("body",notificationMessage);

            JSONObject dataObj = new JSONObject();


            jsonObject.put("notification",notificationObj);
            jsonObject.put("to",fcmToken);
            // Tạo kết nối HTTP để gửi thông báo đẩy
            OkHttpClient client = new OkHttpClient();
            MediaType JSON = MediaType.get("application/json; charset=utf-8");
            Log.d("FCM", "Preparing to send notification with token: " + fcmToken+" "+jsonObject.toString());
            //RequestBody body = RequestBody.create(JSON,sending.toString());
//            Request request = new Request.Builder()
//                    .url("https://fcm.googleapis.com/v1/projects/chattyparty-7d883/messages:send")
//                    .post(body)
//                    .addHeader("Authorization", "Bearer ya29.a0AfB_byDZuHXk5Ow5QN2SfBGUr2dIdS8cUbPEcVAuo3aMV9GPI40r8IoXNTXDJ74C8AMLZJrx1R3xLPuRQIsl8i-kPH_XnFzHpwUXCOyT23ESgD86JDy3LdhmYRZxjHGxef3YqrWVIzOhMrAM3snULoyLuGeEBBDyr88B5gaCgYKAQsSARASFQHsvYlsowK03wNAsul0lnBu_GHJ3w0173") // Thay YOUR_SERVER_KEY bằng server key của bạn từ Firebase Console
//                    .build();
            RequestBody body = RequestBody.create(JSON,jsonObject.toString());
            Request request = new Request.Builder()
                    .url("https://fcm.googleapis.com/fcm/send")
                    .post(body)
                    .addHeader("Authorization", "Bearer AAAADPhjAMY:APA91bEwZVIVDganFUpe4eQqkhwP8oY8c8klxwe5JSH9jKb0UZBxjh7Y5AiKaIMZZTNrAEc-GK07fDO-tI1kHGxhjfZoUmYhp6q_ZoOhzfA3IeVdph3eaudoQng2XjNZrWv63IQ7gINs") // Thay YOUR_SERVER_KEY bằng server key của bạn từ Firebase Console
                    .build();
            // Thực hiện request để gửi thông báo đẩy
            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                    // Handle error
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    Log.d("FCM", response.body().string());
                }
            }
            );

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class ListMessageAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Context context;
    private Conversation conversation;
    private HashMap<String, String> bitmapAvata;
    private HashMap<String, DatabaseReference> bitmapAvataDB;
    private String bitmapAvataUser;

    public ListMessageAdapter(Context context, Conversation conversation, HashMap<String, String> bitmapAvata, String bitmapAvataUser) {
        this.context = context;
        this.conversation = conversation;
        this.bitmapAvata = bitmapAvata;
        this.bitmapAvataUser = bitmapAvataUser;
        bitmapAvataDB = new HashMap<>();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == ChatActivity.VIEW_TYPE_FRIEND_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_friend, parent, false);
            return new ItemMessageFriendHolder(view);
        } else if (viewType == ChatActivity.VIEW_TYPE_USER_MESSAGE) {
            View view = LayoutInflater.from(context).inflate(R.layout.rc_item_message_user, parent, false);
            return new ItemMessageUserHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemMessageFriendHolder) {
            ((ItemMessageFriendHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).text);
            String currentAvata = bitmapAvata.get(conversation.getListMessageData().get(position).idSender);
            if (currentAvata != null) {
                Glide.with(holder.itemView).load(currentAvata).into(((ItemMessageFriendHolder) holder).avata);
            } else {
                final String id = conversation.getListMessageData().get(position).idSender;
                if(bitmapAvataDB.get(id) == null){
                    bitmapAvataDB.put(id, FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("users/" + id + "/avata"));
                    bitmapAvataDB.get(id).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (dataSnapshot.getValue() != null) {
                                String avataStr = (String) dataSnapshot.getValue();
                                if(!avataStr.equals(StaticConfig.AVATA)) {

                                    ChatActivity.bitmapAvataFriend.put(id, avataStr);
                                }else{
                                    ChatActivity.bitmapAvataFriend.put(id, StaticConfig.AVATA);
                                }
                                notifyDataSetChanged();
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }
        } else if (holder instanceof ItemMessageUserHolder) {
            ((ItemMessageUserHolder) holder).txtContent.setText(conversation.getListMessageData().get(position).text);
            if (bitmapAvataUser != null) {
                Glide.with(holder.itemView).load(StaticConfig.AVATA).into(((ItemMessageUserHolder) holder).avata);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return conversation.getListMessageData().get(position).idSender.equals(StaticConfig.UID) ? ChatActivity.VIEW_TYPE_USER_MESSAGE : ChatActivity.VIEW_TYPE_FRIEND_MESSAGE;
    }

    @Override
    public int getItemCount() {
        return conversation.getListMessageData().size();
    }
}

class ItemMessageUserHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public CircleImageView avata;

    public ItemMessageUserHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentUser);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView2);
    }
}

class ItemMessageFriendHolder extends RecyclerView.ViewHolder {
    public TextView txtContent;
    public CircleImageView avata;

    public ItemMessageFriendHolder(View itemView) {
        super(itemView);
        txtContent = (TextView) itemView.findViewById(R.id.textContentFriend);
        avata = (CircleImageView) itemView.findViewById(R.id.imageView3);
    }
}