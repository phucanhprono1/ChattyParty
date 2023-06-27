package com.example.chattyparty;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.chattyparty.data.StaticConfig;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;



public class ChangeProfile extends AppCompatActivity {
    private static final int REQUEST_CODE = 101;
    private Uri filePath;
    private ImageView imgAvt;
    private String avtPath="";
    private final int PICK_IMAGE_REQUEST = 70;
    FirebaseStorage storage;
    StorageReference storageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance("https://chattyparty-7d883-default-rtdb.asia-southeast1.firebasedatabase.app/").getReference("users");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_profile);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        EditText name = findViewById(R.id.editUsername);
        EditText city = findViewById(R.id.editCity);
        EditText country = findViewById(R.id.editCountry);
        EditText profession = findViewById(R.id.editProfession);
        TextView email = findViewById(R.id.email);
        TextView bio = findViewById(R.id.editBio);
        imgAvt = findViewById(R.id.profile_image);

        name.setText(StaticConfig.NAME);
        city.setText(StaticConfig.CITY);
        country.setText(StaticConfig.COUNTRY);
        profession.setText(StaticConfig.PROFESSION);
        email.setText(StaticConfig.EMAIL);
        bio.setText(StaticConfig.BIO);
        Glide.with(getApplicationContext()).load(Uri.parse(StaticConfig.AVATA)).override(100,100).into(imgAvt);

        storage =FirebaseStorage.getInstance();
        storageRef = storage.getReference();
        imgAvt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        findViewById(R.id.btnSave).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = name.getText().toString();
                String usercity= city.getText().toString();
                String usercountry = country.getText().toString();
                String userprofession = profession.getText().toString();
                String userbio = bio.getText().toString();
                usersRef.child(user.getUid()).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        usersRef.child(user.getUid()).child("name").setValue(username);
                        usersRef.child(user.getUid()).child("city").setValue(usercity);
                        usersRef.child(user.getUid()).child("country").setValue(usercountry);
                        usersRef.child(user.getUid()).child("profession").setValue(userprofession);
                        usersRef.child(user.getUid()).child("bio").setValue(userbio);

                        if(!avtPath.equalsIgnoreCase("")){
                            usersRef.child(user.getUid()).child("avata").setValue(avtPath);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                Intent i = new Intent(ChangeProfile.this, MainProfile.class);
                startActivity(i);
            }
        });


    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null )
        {
            filePath = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                imgAvt.setImageBitmap(bitmap);
                uploadImage(); // Call the uploadImage() method immediately after selecting an image
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }
    private void uploadImage() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            String pathString ="avatar/"+ UUID.randomUUID().toString();
            StorageReference ref = storageRef.child(pathString);
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
}