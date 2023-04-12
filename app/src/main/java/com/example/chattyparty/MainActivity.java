package com.example.chattyparty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.facebook.login.LoginManager;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.squareup.picasso.Picasso;

public class MainActivity extends AppCompatActivity {
    ImageView avatar;
    TextView username;
    Button logout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        avatar = findViewById(R.id.avatar);
        Glide.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/chattyparty-7d883.appspot.com/o/default-profile-icon-5.jpg?alt=media&token=709f372e-e2a0-44ca-8c22-0bb47e710f8c")
                .override(120,120).placeholder(R.drawable.placeholder).into(avatar);
        username = findViewById(R.id.username);
        logout = (Button) findViewById(R.id.btnLogout);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        findViewById(R.id.change_profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this,ChangeProfile.class);
                startActivity(i);
            }
        });
        usersRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String name = dataSnapshot.child("username").getValue(String.class);
                    username.setText(name);
                    String tmp=dataSnapshot.child("avt").getValue(String.class);
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