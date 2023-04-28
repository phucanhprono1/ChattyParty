package com.example.chattyparty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattyparty.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;
import android.Manifest;

public class SignUp extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private EditText mUsernameField;
    private EditText mEmailField;
    private EditText mPasswordField;
    private Button btnChoose, btnUpload;
    private ImageView imageView;
    //Firebase
    FirebaseStorage storage;
    StorageReference storageReference;
    private Uri filePath;
    private String avtPath;
    private final int PICK_IMAGE_REQUEST = 71;
    ImageButton btnCamera;
    TextView repassword;
    User user;
    private final int REQUEST_CODE=1000;
    private static final int PERMISSION_CODE = 1000;
    private static final int IMAGE_CAMERA_CODE = 1001;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();
        mUsernameField = findViewById(R.id.editTextUsername);
        mEmailField = findViewById(R.id.editTextEmail1);
        mPasswordField = findViewById(R.id.editTextPassword1);
        repassword = findViewById(R.id.repassword);

        //Set avatar
        storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        //Initialize Views
        btnChoose = (Button) findViewById(R.id.btnChoose);
        btnCamera = (ImageButton) findViewById(R.id.btnCamera);
        btnUpload = (Button) findViewById(R.id.btnUpload);
        imageView = (ImageView) findViewById(R.id.imgView);
        Glide.with(getApplicationContext()).load("https://firebasestorage.googleapis.com/v0/b/chattyparty-7d883.appspot.com/o/default-profile-icon-5.jpg?alt=media&token=709f372e-e2a0-44ca-8c22-0bb47e710f8c").override(100,100)
                .placeholder(R.drawable.placeholder).error(R.drawable.placeholder)
                .into(imageView);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();

            }
        });
        btnChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });
        ImageButton camera = findViewById(R.id.btnCamera);
        btnUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });


        findViewById(R.id.textViewSignIn1).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToLogin();
            }
        });

        findViewById(R.id.buttonRegister).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameField.getText().toString();
                String email = mEmailField.getText().toString();
                String password = mPasswordField.getText().toString();
                String repass = repassword.getText().toString();

                if (TextUtils.isEmpty(username)) {
                    mUsernameField.setError("Username is required.");
                    return;
                }

                if (TextUtils.isEmpty(email)) {
                    mEmailField.setError("Email is required.");
                    return;
                }

                if (TextUtils.isEmpty(password)) {
                    mPasswordField.setError("Password is required.");
                    return;
                }
                if (password.length() < 6) {
                    mPasswordField.setError("Password must be >= 6 characters.");
                    return;
                }
                if(!password.equals(repass)){
                    repassword.setError("Password is not match.");
                    return;
                }
                if(filePath == null){
                    Toast.makeText(SignUp.this, "Please choose your avatar.", Toast.LENGTH_SHORT).show();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    saveUserToFirebase(username, email);
                                    goToMainActivity();
                                } else {
                                    Toast.makeText(SignUp.this, "Register failed. Please try again later.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }
    private static final int CAMERA_REQUEST_CODE = 2;
    private void takePicture() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, CAMERA_REQUEST_CODE);
        }
    }







    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePicture();
                } else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else if (requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);

            // Save image to storage and get the filepath
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            String fileName = "avatar_" + System.currentTimeMillis() + ".jpg";
            StorageReference storageRef = storageReference.child("avatar/" + fileName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] dataBytes = baos.toByteArray();
            UploadTask uploadTask = storageRef.putBytes(dataBytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                    storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {
                            avtPath = uri.toString();
                            filePath = uri;
                        }
                    });
                }
            });
        }
    }

    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            String pathString ="avatar/"+ UUID.randomUUID().toString();
            StorageReference ref = storageReference.child(pathString);
            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Uploaded", Toast.LENGTH_SHORT).show();
                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    avtPath = uri.toString();
                                    // Do something with imageURL
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getApplicationContext(), "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }
    private void saveUserToFirebase(String username, String email) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        String userId = mAuth.getCurrentUser().getUid();

        user = new User(userId, username, email, avtPath);
        usersRef.child(userId).setValue(user);

    }

    private void goToLogin() {
        Intent intent = new Intent(SignUp.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }

    private void goToMainActivity() {
        Intent intent = new Intent(SignUp.this, MainProfile.class);

        startActivity(intent);
        finish();
    }
}