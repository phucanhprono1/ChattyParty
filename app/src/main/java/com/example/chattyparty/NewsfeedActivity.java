package com.example.chattyparty;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class NewsfeedActivity extends AppCompatActivity {
    private static final int REQUEST_CODE=101;
    ImageView addPostImage,sendPost;
    EditText inputPost;
    Uri imageUri;
    String profileImageUrl,userName;
    DatabaseReference PostRef,mUserRef;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    StorageReference postImageRef;
    ProgressDialog mLoadingBar;
    FirebaseRecyclerAdapter<Posts,MyViewHolder>adapter;
    FirebaseRecyclerOptions<Posts>options;
    RecyclerView recyclerView;

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
                holder.postDesc.setText(model.getPostDesc());
                holder.timeAgo.setText(model.getDatePost());
                holder.username.setText(model.getUsername());
//
//░█████╗░██╗░░░██╗██╗░░░██╗  ████████╗░█████╗░██╗
//██╔══██╗██║░░░██║██║░░░██║  ╚══██╔══╝██╔══██╗██║
//██║░░╚═╝██║░░░██║██║░░░██║  ░░░██║░░░██║░░██║██║
//██║░░██╗██║░░░██║██║░░░██║  ░░░██║░░░██║░░██║██║
//╚█████╔╝╚██████╔╝╚██████╔╝  ░░░██║░░░╚█████╔╝██║
//░╚════╝░░╚═════╝░░╚═════╝░  ░░░╚═╝░░░░╚════╝░╚═╝
//
//██╗░░░██╗░█████╗░██╗
//██║░░░██║██╔══██╗██║
//╚██╗░██╔╝██║░░██║██║
//░╚████╔╝░██║░░██║██║
//░░╚██╔╝░░╚█████╔╝██║
//░░░╚═╝░░░░╚════╝░╚═╝
                Glide.with(holder.itemView).load(model.getPostImageURL()).into(((MyViewHolder)holder).postImage);
                Glide.with(holder.itemView).load(model.getUserProfileImageURL()).into(((MyViewHolder)holder).profileImage);
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
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy hh:mm:ss");
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