package com.example.chattyparty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chattyparty.data.StaticConfig;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;

public class FriendWallActivity extends AppCompatActivity {
    CircleImageView avatar;
    TextView username;
    TextView email;
    TextView city;
    TextView country;
    TextView profession;
    TextView bio;

    DatabaseReference usersRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String friendId = getIntent().getStringExtra("friendId");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_wall);
        avatar = findViewById(R.id.profile_image1);
        username = findViewById(R.id.editUsername1);
        email = findViewById(R.id.email1);
        city = findViewById(R.id.editCity1);
        country = findViewById(R.id.editCountry1);
        profession = findViewById(R.id.editProfession1);
        bio = findViewById(R.id.editBio1);

        Glide.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/chattyparty-7d883.appspot.com/o/default-profile-icon-5.jpg?alt=media&token=709f372e-e2a0-44ca-8c22-0bb47e710f8c")
                .override(120,120).placeholder(R.drawable.placeholder).into(avatar);
        usersRef.child(friendId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("name").getValue(String.class);
                    StaticConfig.NAME = name;
                    username.setText(name);
                    String uemail = dataSnapshot.child("email").getValue(String.class);
                    email.setText(uemail);
                    StaticConfig.EMAIL = uemail;
                    String ucity = dataSnapshot.child("city").getValue(String.class);
                    city.setText(ucity);
                    String ucountry = dataSnapshot.child("country").getValue(String.class);
                    country.setText(ucountry);
                    String uprofession = dataSnapshot.child("profession").getValue(String.class);
                    profession.setText(uprofession);
                    String ubio = dataSnapshot.child("bio").getValue(String.class);
                    bio.setText(ubio);
                    String tmp=dataSnapshot.child("avata").getValue(String.class);
//                    StaticConfig.AVATA = tmp;
//                    Uri avt =  Uri.parse(tmp);
                    Glide.with(getApplicationContext()).load(tmp).override(150,150)
                            .placeholder(R.drawable.placeholder)
                            .error(R.drawable.placeholder).into(avatar);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Log.e("TAG", "Error: " + databaseError.getMessage());
            }
        });
    }
}