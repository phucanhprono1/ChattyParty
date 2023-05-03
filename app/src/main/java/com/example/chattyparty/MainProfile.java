package com.example.chattyparty;



import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.chattyparty.data.StaticConfig;
import com.example.chattyparty.service.ServiceUtils;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import de.hdodenhof.circleimageview.CircleImageView;
//import com.squareup.picasso.Picasso;

public class MainProfile extends AppCompatActivity {
    CircleImageView avatar;
    TextView username;
    TextView email;
    TextView city;
    TextView country;
    TextView profession;
    TextView bio;
    Button logout;
    Button chat;
    Button feed;
    Button friend_request;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_profile);

        avatar = findViewById(R.id.profile_image);
        username = findViewById(R.id.editUsername);
        email = findViewById(R.id.email);
        city = findViewById(R.id.editCity);
        country = findViewById(R.id.editCountry);
        profession = findViewById(R.id.editProfession);
        bio = findViewById(R.id.editBio);
        logout = (Button) findViewById(R.id.btnLogout);
        chat = (Button) findViewById(R.id.btnchat);
        feed = (Button) findViewById(R.id.btnfeed);
        Glide.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/chattyparty-7d883.appspot.com/o/default-profile-icon-5.jpg?alt=media&token=709f372e-e2a0-44ca-8c22-0bb47e710f8c")
                .override(120,120).placeholder(R.drawable.placeholder).into(avatar);

        chat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainProfile.this,MainActivity.class);
                startActivity(i);
            }
        });


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        StaticConfig.UID = user.getUid();
        findViewById(R.id.btnchange_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainProfile.this,ChangeProfile.class);
                startActivity(i);
            }
        });
        feed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainProfile.this,NewsfeedActivity.class);
                startActivity(i);
            }
        });
        usersRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
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
                    StaticConfig.AVATA = tmp;
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
        //}

        friend_request = findViewById(R.id.btn_friend_requests);
        friend_request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainProfile.this,FriendRequestActivity.class);
                startActivity(i);
            }
        });
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logout(view);
            }
        });
    }
    private void goLoginScreen() {
        Intent intent = new Intent(this, LoginOptionActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    public void logout(View view) {
        FirebaseAuth.getInstance().signOut();
        LoginManager.getInstance().logOut();
        goLoginScreen();
    }
}