package com.example.chattyparty;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import de.hdodenhof.circleimageview.CircleImageView;

public class MyViewHolder extends RecyclerView.ViewHolder {
    CircleImageView profileImage;
    ImageView postImage,likeImage,commentsImage;
    TextView username,timeAgo,postDesc,likeCounter,commentsCounter;
    public MyViewHolder( View itemView) {
        super(itemView);
        profileImage=itemView.findViewById(R.id.profileImagePost);
        postImage=itemView.findViewById(R.id.postImage);
        likeImage=itemView.findViewById(R.id.likeImage);
        commentsImage=itemView.findViewById(R.id.commentsImage);
        username=itemView.findViewById(R.id.profileUsernamePost);
        timeAgo=itemView.findViewById(R.id.timeAgo);
        postDesc=itemView.findViewById(R.id.postDesc);
        likeCounter=itemView.findViewById(R.id.likeCounter);
        commentsCounter=itemView.findViewById(R.id.commentsCounter);
    }
}