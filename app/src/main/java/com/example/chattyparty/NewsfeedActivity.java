package com.example.chattyparty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattyparty.model.Comment;
import com.example.chattyparty.model.Posts;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
//import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.TimeZone;

public class NewsfeedActivity extends AppCompatActivity {
    private static final int REQUEST_CODE=101;
    ImageView addPostImage,sendPost;
    EditText inputPost;
    Uri imageUri;
    String profileImageUrl,userName;
    DatabaseReference PostRef,mUserRef, likeRef,CommentRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    StorageReference postImageRef;
    ProgressDialog mLoadingBar;
    FirebaseRecyclerAdapter<Posts,MyViewHolder>adapter;
    FirebaseRecyclerOptions<Posts>options;
    RecyclerView recyclerView;
    FirebaseRecyclerOptions<Comment>CommentOption;
    FirebaseRecyclerAdapter<Comment,CommentViewHolder>CommentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_newsfeed);

        addPostImage=findViewById(R.id.addPostImage);
        sendPost=findViewById(R.id.sendPost);
        inputPost=findViewById(R.id.inputAddPost);

        mAuth=FirebaseAuth.getInstance();
        mUser= mAuth.getCurrentUser();
        mUserRef= FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("users");
        PostRef= FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("posts");
        likeRef= FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("likes");
        CommentRef= FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference().child("comments");

        postImageRef= FirebaseStorage.getInstance().getReference().child("PostImages");

        mLoadingBar=new ProgressDialog(this);
        recyclerView=findViewById(R.id.recyclerPost);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        LoadPosts();

        mUserRef.child(mUser.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if(snapshot.exists()){
                    profileImageUrl=snapshot.child("avata").getValue().toString();
                    userName=snapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        sendPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPost();
            }
        });
        addPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/");
                startActivityForResult(intent,REQUEST_CODE);
            }
        });
    }

    private void LoadPosts() {
        options=new FirebaseRecyclerOptions.Builder<Posts>().setQuery(PostRef,Posts.class).build();
        adapter=new FirebaseRecyclerAdapter<Posts, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(MyViewHolder holder, int position,  Posts model) {
                String postKey=getRef(position).getKey();
                holder.postDesc.setText(model.getPostDesc());
                String timeAgo=caculateTimeAgo(model.getDatePost());
                holder.timeAgo.setText(timeAgo);
                holder.username.setText(model.getUsername());
//                Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chattyparty-7d883.appspot.com/o/PostImages%2F7tTCfBPuQMZ9IdvpETW2sQtpw3y229-04-2023%2011%3A59%3A55?alt=media&token=771c30bd-ed25-4cb2-ab55-2f320d91afa8").into(holder.postImage);
//                Picasso.get().load(model.getUserProfileImageURL()).into(holder.profileImage);
                Glide.with(holder.itemView).load(model.getPostImageURL()).into(((MyViewHolder)holder).postImage);
                Glide.with(holder.itemView).load(model.getUserProfileImageURL()).into(((MyViewHolder)holder).profileImage);
                holder.countLikes(postKey,mUser.getUid(),likeRef);
                holder.countComments(postKey,mUser.getUid(),CommentRef);
                holder.likeImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        likeRef.child(postKey).child(mUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if(snapshot.exists()){
                                    likeRef.child(postKey).child(mUser.getUid()).removeValue();
                                    holder.likeImage.setColorFilter(Color.GRAY);
                                    notifyDataSetChanged();
                                }
                                else{
                                    likeRef.child(postKey).child(mUser.getUid()).setValue("like");
                                    holder.likeImage.setColorFilter(Color.BLUE);
                                    notifyDataSetChanged();
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Toast.makeText(NewsfeedActivity.this,""+error.getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
                holder.sendComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String comment=holder.inputComments.getText().toString();
                        if(comment.isEmpty()){
                            Toast.makeText(NewsfeedActivity.this,"Comment something before sending",Toast.LENGTH_SHORT).show();
                        }
                        else AddComment(holder,postKey,CommentRef,mUser.getUid(),comment);
                    }
                });
                LoadComments(postKey);
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_post,parent,false);
                return new MyViewHolder(view);
            }
        };
        adapter.startListening();
        recyclerView.setAdapter(adapter);
    }

    private void LoadComments(String postKey) {
        MyViewHolder.recyclerView.setLayoutManager(new LinearLayoutManager(NewsfeedActivity.this));
        CommentOption=new FirebaseRecyclerOptions.Builder<Comment>().setQuery(CommentRef.child(postKey), Comment.class).build();
        CommentAdapter=new FirebaseRecyclerAdapter<Comment, CommentViewHolder>(CommentOption) {
            @Override
            protected void onBindViewHolder(@NonNull CommentViewHolder holder, int position, @NonNull Comment model) {
                Glide.with(holder.itemView).load(model.getProfileImageURL()).into(((CommentViewHolder)holder).profileImage);
                holder.username.setText(model.getUsername());
                holder.comment.setText(model.getComment());
            }

            @NonNull
            @Override
            public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.single_view_comment,parent,false);
                return new CommentViewHolder(view);
            }
        };
        CommentAdapter.startListening();
        MyViewHolder.recyclerView.setAdapter(CommentAdapter);
    }

    private void AddComment(MyViewHolder holder, String postKey, DatabaseReference commentRef, String uid, String comment) {
        HashMap hashMap=new HashMap();
        hashMap.put("username",userName);
        hashMap.put("profileImageURL",profileImageUrl);
        hashMap.put("comment",comment);

        commentRef.child(postKey).child(uid).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if(task.isSuccessful()){
                    Toast.makeText(NewsfeedActivity.this,"Comment Added",Toast.LENGTH_SHORT).show();
                    adapter.notifyDataSetChanged();
                    holder.inputComments.setText(null);
                }
                else{
                    Toast.makeText(NewsfeedActivity.this,""+task.getException().toString(),Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private String caculateTimeAgo(String datePost) {
        SimpleDateFormat sdf=new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
//        sdf.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        try{
            long time = sdf.parse(datePost).getTime();
            long now=System.currentTimeMillis();
            CharSequence ago= DateUtils.getRelativeTimeSpanString(time,now,DateUtils.MINUTE_IN_MILLIS);
            return ago+"";
        } catch (ParseException e){
            e.printStackTrace();
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_CODE&&resultCode==RESULT_OK&&data!=null){
            imageUri=data.getData();
            addPostImage.setImageURI(imageUri);
        }
    }

    private void addPost() {
        String postDesc=inputPost.getText().toString();
        if (postDesc.isEmpty() || imageUri==null){
            Toast.makeText(this,"Please add an image and a description",Toast.LENGTH_SHORT).show();
        }
        else{
            mLoadingBar.setTitle("Posting");
            mLoadingBar.setCanceledOnTouchOutside(false);
            mLoadingBar.show();

            Date date=new Date();
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
            String strDate = formatter.format(date);

            postImageRef.child(mUser.getUid()+strDate).putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if(task.isSuccessful()){
                        postImageRef.child(mUser.getUid()+strDate).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {


                                HashMap hashMap=new HashMap();
                                hashMap.put("datePost",strDate);
                                hashMap.put("postImageUrl",uri.toString());
                                hashMap.put("postDesc",postDesc);
                                hashMap.put("userProfileImageUrl",profileImageUrl);
                                hashMap.put("username",userName);
                                PostRef.child(mUser.getUid()+strDate).updateChildren(hashMap).addOnCompleteListener(new OnCompleteListener() {
                                    @Override
                                    public void onComplete(@NonNull Task task) {
                                        if(task.isSuccessful()){
                                            mLoadingBar.dismiss();
                                            addPostImage.setImageResource(R.drawable.ic_add_post_image);
                                            inputPost.setText("");
                                        }
                                        else{
                                            mLoadingBar.dismiss();
                                            Toast.makeText(NewsfeedActivity.this,""+task.getException().toString(),Toast.LENGTH_SHORT);
                                        }
                                    }
                                });
                            }
                        });
                    }
                    else{
                        mLoadingBar.dismiss();
                        Toast.makeText(NewsfeedActivity.this,""+task.getException().toString(),Toast.LENGTH_SHORT);
                    }
                }
            });
        }
    }
}