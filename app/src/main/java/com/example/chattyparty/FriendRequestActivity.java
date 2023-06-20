package com.example.chattyparty;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattyparty.data.FriendDB;
import com.example.chattyparty.data.FriendRequestDB;
import com.example.chattyparty.data.StaticConfig;
import com.example.chattyparty.model.Friend;
import com.example.chattyparty.model.FriendRequest;
import com.example.chattyparty.model.ListFriend;
import com.example.chattyparty.service.ServiceUtils;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class FriendRequestActivity extends AppCompatActivity  {
    boolean connected = ServiceUtils.isNetworkConnected(FriendRequestActivity.this);
    List<FriendRequest> friendRequests = new ArrayList<>();
    DatabaseReference userRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
    DatabaseReference friendRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend");

    DatabaseReference messageRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("message");
    DatabaseReference friendRequestRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend_requests");
    FriendRequestAdapter friendRequestAdapter;
    FriendRequestDB friendRequestDB;
    SwipeRefreshLayout swipeRefreshLayout;
    private ListFriend dataListFriend = null;
    private ArrayList<String> listFriendID = null;
    private ArrayList<String> listFriendID1 = null;
    FriendDB friendDB;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        RecyclerView recyclerView = findViewById(R.id.recyclerView_friend_request);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout_friend_request);
        friendRequestDB = new FriendRequestDB(getBaseContext());
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                friendRequests.clear();
                StaticConfig.LIST_FRIEND_REQUEST.clear();

                friendRef.child(StaticConfig.UID).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.getValue() != null) {
                            HashMap mapRecord = (HashMap) dataSnapshot.getValue();
                            Iterator listKey = mapRecord.keySet().iterator();
                            while (listKey.hasNext()) {
                                String key = listKey.next().toString();
                                Log.d("FriendRequestActivity", "onDataChange: " + mapRecord.get(key).toString());
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "null", Toast.LENGTH_SHORT);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });

                swipeRefreshLayout.setRefreshing(false);
            }

        });
        FirebaseRecyclerOptions<FriendRequest> options = new FirebaseRecyclerOptions.Builder<FriendRequest>()
                .setQuery(friendRequestRef.child(StaticConfig.UID), FriendRequest.class)
                .build();
        friendRequestAdapter = new FriendRequestAdapter(options, new FriendRequestAdapter.AcceptClickListener() {
            @Override
            public void onAcceptClick(FriendRequest friendRequest) {
                // Xử lý sự kiện khi người dùng chấp nhận lời mời
            }
        }, new FriendRequestAdapter.DeleteClickListener() {
            @Override
            public void onDeleteClick(FriendRequest friendRequest) {
                // Xử lý sự kiện khi người dùng xóa lời mời
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        recyclerView.setAdapter(friendRequestAdapter);


        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                StaticConfig.LIST_FRIEND_REQUEST.clear();
                Intent intent = new Intent(FriendRequestActivity.this, MainProfile.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });


    }
    private void addFriend(final String idFriend, boolean isIdFriend) {
        if (idFriend != null) {
            if (isIdFriend) {
                friendRef.child(StaticConfig.UID).push().setValue(idFriend)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    addFriend(idFriend, false);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//
                            }
                        });
            } else {
                friendRef.child(idFriend).push().setValue(StaticConfig.UID).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    addFriend(null, false);
                                }
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
//
                            }
                        });
            }
        } else {
//
//                Toast.makeText(getContext(), "Add friend success", Toast.LENGTH_SHORT);
        }
    }


    @Override
    public void onBackPressed() {
        StaticConfig.LIST_FRIEND_REQUEST.clear();
        Intent intent = new Intent(FriendRequestActivity.this, MainProfile.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        super.onBackPressed();
    }
//    @Override
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            StaticConfig.LIST_FRIEND_REQUEST.clear();
//            Toast.makeText(getApplicationContext(), "Clear", Toast.LENGTH_SHORT).show();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }
    @Override
    protected void onStart() {
        super.onStart();
        friendRequestAdapter.startListening();
    }

    @Override
    protected void onStop() {
        super.onStop();
        friendRequestAdapter.stopListening();
    }
}

class FriendRequestAdapter extends FirebaseRecyclerAdapter<FriendRequest, FriendRequestAdapter.ViewHolder> {
    DatabaseReference friendRequestRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend_requests");
    private AcceptClickListener acceptClickListener;
    private DeleteClickListener deleteClickListener;

    public interface AcceptClickListener {
        void onAcceptClick(FriendRequest friendRequest);
    }

    public interface DeleteClickListener {
        void onDeleteClick(FriendRequest friendRequest);
    }

    public FriendRequestAdapter(@NonNull FirebaseRecyclerOptions<FriendRequest> options, AcceptClickListener acceptClickListener, DeleteClickListener deleteClickListener) {
        super(options);
        this.acceptClickListener = acceptClickListener;
        this.deleteClickListener = deleteClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull ViewHolder holder, int position, @NonNull FriendRequest friendRequest) {
        Glide.with(holder.itemView).load(friendRequest.getSenderImage()).into(holder.imageViewProfile);
        holder.textViewName.setText(friendRequest.getSenderName());
        boolean connected = ServiceUtils.isNetworkConnected(holder.itemView.getContext());

        holder.buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected && acceptClickListener != null) {
                    acceptClickListener.onAcceptClick(friendRequest);
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", "accepted");

                    friendRequestRef.child(StaticConfig.UID).child(friendRequest.getSender()).updateChildren(map);
                    holder.buttonAccept.setVisibility(View.GONE);
                    holder.buttonDelete.setVisibility(View.GONE);
                    holder.notification.setVisibility(View.VISIBLE);
                    holder.notification.setText("Friend Request Accepted");
                }
            }
        });

        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (connected && deleteClickListener != null) {
                    deleteClickListener.onDeleteClick(friendRequest);
                    Map<String, Object> map = new HashMap<>();
                    map.put("status", "rejected");
                    friendRequestRef.child(StaticConfig.UID).child(friendRequest.getSender()).updateChildren(map);

                    holder.buttonAccept.setVisibility(View.GONE);
                    holder.buttonDelete.setVisibility(View.GONE);
                    holder.notification.setVisibility(View.VISIBLE);
                    holder.notification.setText("Friend Request Rejected");
                }
            }
        });
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewProfile;
        TextView textViewName;
        TextView textViewMessage;
        Button buttonAccept;
        Button buttonDelete;
        TextView notification;

        public ViewHolder(View itemView) {
            super(itemView);
            imageViewProfile = itemView.findViewById(R.id.imageView_profile);
            textViewName = itemView.findViewById(R.id.textView_name);
            textViewMessage = itemView.findViewById(R.id.textView_message);
            buttonAccept = itemView.findViewById(R.id.button_accept);
            buttonDelete = itemView.findViewById(R.id.button_delete);
            notification = itemView.findViewById(R.id.notification_background);
        }
    }
}