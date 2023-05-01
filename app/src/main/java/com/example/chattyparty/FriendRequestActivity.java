package com.example.chattyparty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattyparty.data.StaticConfig;
import com.example.chattyparty.model.FriendRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FriendRequestActivity extends AppCompatActivity {

    List<FriendRequest> friendRequests = new ArrayList<>();
    DatabaseReference friendRequestRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend_requests");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_request);
        RecyclerView recyclerView = findViewById(R.id.recyclerView_friend_request);
        FriendRequestAdapter friendRequestAdapter = new FriendRequestAdapter(friendRequests);
        friendRequestRef.child(StaticConfig.UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {


                for(DataSnapshot dataSnapshot : snapshot.getChildren()){
                    FriendRequest friendRequest = dataSnapshot.getValue(FriendRequest.class);
                    friendRequests.add(friendRequest);
                }
                friendRequestAdapter.notifyDataSetChanged();

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(FriendRequestActivity.this, LinearLayoutManager.VERTICAL, false));
        recyclerView.setAdapter(friendRequestAdapter);
    }
}
class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {
    //DatabaseReference friendRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend");
    DatabaseReference friendRequestRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("friend_requests");
    private List<FriendRequest> friendRequests;

    public FriendRequestAdapter(List<FriendRequest> friendRequests) {
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
        holder.buttonAccept.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(friendRequestRef.child(StaticConfig.UID).child(friendRequest.getSender()).child("status")!=null){
                    friendRequestRef.child(StaticConfig.UID).child(friendRequest.getSender()).child("status").setValue("accepted");
                    holder.buttonAccept.setVisibility(View.GONE);
                    holder.buttonDelete.setVisibility(View.GONE);
                    holder.notification.setVisibility(View.VISIBLE);
                    holder.notification.setText("Friend Request Accepted");
                }

                // handle accept button click event
            }
        });
        holder.buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                friendRequestRef.child(StaticConfig.UID).child(friendRequest.getSender()).child("status").setValue("rejected");

                holder.buttonAccept.setVisibility(View.GONE);
                holder.buttonDelete.setVisibility(View.GONE);
                holder.notification.setVisibility(View.VISIBLE);
                holder.notification.setText("Friend Request Rejected");
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