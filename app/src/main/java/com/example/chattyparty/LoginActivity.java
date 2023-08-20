package com.example.chattyparty;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.chattyparty.data.StaticConfig;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;

public class LoginActivity extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private EditText mEmailField;
    private EditText mPasswordField;
    DatabaseReference usersRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mAuth = FirebaseAuth.getInstance();
        mEmailField = findViewById(R.id.editTextEmail);
        mPasswordField = findViewById(R.id.editTextPassword);

        findViewById(R.id.textViewForgotPassword).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToForgotPassword();
            }
        });

        findViewById(R.id.textViewRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToRegister();
            }
        });

        findViewById(R.id.buttonSignIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();

                if (TextUtils.isEmpty(email)) {
                    mEmailField.setError("Email is required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPasswordField.setError("Password is required.");
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    updateUI(user);
                                } else {
                                    Toast.makeText(LoginActivity.this, "Incorrect email or password.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
    private void saveOAuthToken(String token) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("oauth_token", token);
        editor.apply();
    }
    private void goToForgotPassword() {
        Intent intent = new Intent(LoginActivity.this, ForgotPasswordActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToRegister() {
        Intent intent = new Intent(LoginActivity.this, SignUp.class);
        startActivity(intent);
        finish();
    }



    private void updateUI(FirebaseUser user) {
        StaticConfig.UID = user.getUid();
        String userId = user.getUid();
        user.getIdToken(true)  // This line retrieves the Firebase ID token.
                .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                    @Override
                    public void onComplete(@NonNull Task<GetTokenResult> task) {
                        if (task.isSuccessful()) {
                            String idToken = task.getResult().getToken();

                            // Save the Firebase ID token (OAuth2 token) to SharedPreferences
                            saveOAuthToken(idToken);

                            FirebaseMessaging.getInstance().getToken()
                                    .addOnCompleteListener(new OnCompleteListener<String>() {
                                        @Override
                                        public void onComplete(@NonNull Task<String> task) {
                                            if (!task.isSuccessful()) {
                                                Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                                return;
                                            }

                                            String fcmToken = task.getResult();

                                            // Lưu FCM token vào thông tin người dùng
                                            usersRef.child(userId).child("fcmToken").setValue(fcmToken);

                                            Intent intent = new Intent(LoginActivity.this, MainProfile.class);
                                            startActivity(intent);
                                            finish();
                                        }
                                    });
                        } else {
                            Log.e(TAG, "Error getting Firebase ID token", task.getException());
                            Toast.makeText(LoginActivity.this, "Error getting ID token.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

}