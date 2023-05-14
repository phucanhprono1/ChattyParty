package com.example.chattyparty;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
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
import com.example.chattyparty.data.StaticConfig;
import com.example.chattyparty.model.Friend;
import com.example.chattyparty.model.FriendRequest;
import com.example.chattyparty.model.ListFriend;
import com.example.chattyparty.service.ServiceUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
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

public class FriendRequestActivity extends AppCompatActivity implements FriendRequestAdapter.AcceptClickListener, FriendRequestAdapter.DeleteClickListener {
    boolean connected = ServiceUtils.isNetworkConnected(FriendRequestActivity.this);
    List<FriendRequest> friendRequests = new ArrayList<>();
    DatabaseReference userRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
    DatabaseReference friendRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend");

    DatabaseReference messageRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("message");
    DatabaseReference friendRequestRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend_requests");
    FriendRequestAdapter friendRequestAdapter;
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
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                friendRequests.clear();
                StaticConfig.LIST_FRIEND_REQUEST.clear();
                if(connected){
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
                    friendRequestRef.child(StaticConfig.UID).addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) { // get all friend requests
                            Gson gson = new Gson();
                            for (DataSnapshot child : snapshot.getChildren()) {
                                String json = gson.toJson(child.getValue());
                                FriendRequest friendRequest = gson.fromJson(json, FriendRequest.class);
                                FriendRequest stt = new FriendRequest();
                                stt.setIdRoom(child.child("idRoom").getValue().toString());
                                stt.setId(child.child("id").getValue().toString());
                                stt.setReceiver(child.child("receiver").getValue().toString());
                                stt.setSender(child.child("sender").getValue().toString());

                                stt.setSenderImage(child.child("senderImage").getValue().toString());
                                stt.setSenderName(child.child("senderName").getValue().toString());
                                stt.setAvata(child.child("avata").getValue().toString());
                                stt.setEmail(child.child("email").getValue().toString());
                                friendRequests.add(friendRequest);
                                StaticConfig.LIST_FRIEND_REQUEST.add(stt);
                                friendRequestAdapter.setFriendRequests(StaticConfig.LIST_FRIEND_REQUEST);
                            }


                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                    swipeRefreshLayout.setRefreshing(false);
                }else {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
                    swipeRefreshLayout.setRefreshing(true);
                }
            }

        });
        if(connected){
            friendRequestRef.child(StaticConfig.UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
//                ArrayList<FriendRequest> friendRequests = new ArrayList<>();
                    Gson gson = new Gson();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String json = gson.toJson(child.getValue());
                        FriendRequest friendRequest = gson.fromJson(json, FriendRequest.class);
                        FriendRequest stt = new FriendRequest();
                        stt.setIdRoom(child.child("idRoom").getValue().toString());
                        stt.setId(child.child("id").getValue().toString());
                        stt.setReceiver(child.child("receiver").getValue().toString());
                        stt.setSender(child.child("sender").getValue().toString());

                        stt.setSenderImage(child.child("senderImage").getValue().toString());
                        stt.setSenderName(child.child("senderName").getValue().toString());
                        stt.setAvata(child.child("avata").getValue().toString());
                        stt.setEmail(child.child("email").getValue().toString());

                        friendRequests.add(friendRequest);
                        StaticConfig.LIST_FRIEND_REQUEST.add(stt);
                        friendRequestAdapter.setFriendRequests(StaticConfig.LIST_FRIEND_REQUEST);
                    }

//                recyclerView.setAdapter(friendRequestAdapter);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

            friendRequestRef.child(StaticConfig.UID).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Gson gson = new Gson();
                    for (DataSnapshot child : snapshot.getChildren()) {
                        String json = gson.toJson(child.getValue());
                        FriendRequest friendRequest = gson.fromJson(json, FriendRequest.class);
                        // Check if this friend request already exists in the list


                        if(friendRequest.getStatus().equals("accepted")&&StaticConfig.LIST_FRIEND_ID.contains(friendRequest.getSender())==false){

                            addFriend(friendRequest.getSender(), true);
                            StaticConfig.LIST_FRIEND_ID.add(friendRequest.getSender());


                        } else if (friendRequest.getStatus().equals("rejected")) {

                        }
                    }

                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    // Handle onCancelled event
                }
            });

            friendRequestAdapter = new FriendRequestAdapter(StaticConfig.LIST_FRIEND_REQUEST,this,this);
            recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            friendRequestAdapter.notifyDataSetChanged();
            recyclerView.setAdapter(friendRequestAdapter);
        }
        else {
            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_SHORT).show();
            swipeRefreshLayout.setRefreshing(true);
        }
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
//                Intent intent = new Intent(FriendRequestActivity.this, MainProfile.class);
//                startActivity(intent);
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
    public void onAcceptClick(FriendRequest friendRequest) {

    }

    @Override
    public void onDeleteClick(FriendRequest friendRequest) {

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
}

class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    //DatabaseReference friendRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend");
    DatabaseReference friendRequestRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend_requests");
    private List<FriendRequest> friendRequests;
    private AcceptClickListener acceptClickListener;
    private DeleteClickListener deleteClickListener;

    public interface AcceptClickListener {
        void onAcceptClick(FriendRequest friendRequest);
    }
    public interface DeleteClickListener {
        void onDeleteClick(FriendRequest friendRequest);
    }
    public FriendRequestAdapter(List<FriendRequest> friendRequests, AcceptClickListener acceptClickListener, DeleteClickListener deleteClickListener) {
        this.friendRequests = friendRequests;
        this.acceptClickListener = acceptClickListener;
        this.deleteClickListener = deleteClickListener;
        notifyDataSetChanged();
    }

    public void setFriendRequests(List<FriendRequest> friendRequests) {
        this.friendRequests = friendRequests;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.adapter_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        FriendRequest friendRequest = friendRequests.get(position);
        Glide.with(holder.itemView).load(friendRequest.getSenderImage()).into(holder.imageViewProfile);
        holder.textViewName.setText(friendRequest.getSenderName());
        boolean connected = ServiceUtils.isNetworkConnected(holder.itemView.getContext());
        holder.buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected && acceptClickListener!=null && position != RecyclerView.NO_POSITION){
                    acceptClickListener.onAcceptClick(friendRequest);

                    friendRequestRef.child(StaticConfig.UID).child(friendRequest.getSender()).child("status").setValue("accepted");
                    holder.buttonAccept.setVisibility(View.GONE);
                    holder.buttonDelete.setVisibility(View.GONE);
                    holder.notification.setVisibility(View.VISIBLE);
                    holder.notification.setText("Friend Request Accepted");
                    notifyItemChanged(position);
                }




                // handle accept button click event
            }
        });
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(connected && deleteClickListener!=null && position != RecyclerView.NO_POSITION ){
                    deleteClickListener.onDeleteClick(friendRequest);
                    friendRequestRef.child(StaticConfig.UID).child(friendRequest.getSender()).child("status").setValue("rejected");

                    holder.buttonAccept.setVisibility(View.GONE);
                    holder.buttonDelete.setVisibility(View.GONE);
                    holder.notification.setVisibility(View.VISIBLE);
                    holder.notification.setText("Friend Request Rejected");
                    notifyItemChanged(position);
                }


                // handle delete button click event
            }
        });
    }

    @Override
    public int getItemCount() {
        return friendRequests.size();
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