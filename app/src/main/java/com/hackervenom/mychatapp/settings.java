package com.hackervenom.mychatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class settings extends AppCompatActivity {
    private Button mButton;
    private EditText userName,status;
    private CircleImageView mImageView;
    private String currentUserId;
    private FirebaseAuth mAuth;
    private DatabaseReference RootRef;
    private static final int galleryPick = 1;
    private StorageReference UserProfileImgRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currentUserId = mAuth.getCurrentUser().getUid();
        RootRef = FirebaseDatabase.getInstance().getReference();
        UserProfileImgRef = FirebaseStorage.getInstance().getReference().child("Profile Images");

        mButton = (Button) findViewById(R.id.update_button);
        userName = (EditText) findViewById(R.id.set_user_name);
        status = (EditText) findViewById(R.id.set_status);
        mImageView = (CircleImageView) findViewById(R.id.set_profile_image);
        loadingBar = new ProgressDialog(this);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UpdateSettings();
            }
        });



        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, galleryPick);
            }
        });

        RetrieveUserInfo();

    }

    private void RetrieveUserInfo()
    {
        RootRef.child("USERS").child(currentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name"))&&(dataSnapshot.hasChild("image")))
                        {
                            String rUserName = dataSnapshot.child("name").getValue().toString();
                            String rStatus = dataSnapshot.child("status").getValue().toString();
                            String rImage = dataSnapshot.child("image").getValue().toString();
                            userName.setText(rUserName);
                            status.setText(rStatus);
                            Picasso.get().load(rImage).into(mImageView);
                        }
                        else if((dataSnapshot.exists())&&(dataSnapshot.hasChild("name")))
                        {
                            String rUserName = dataSnapshot.child("name").getValue().toString();
                            String rStatus = dataSnapshot.child("status").getValue().toString();
                            userName.setText(rUserName);
                            status.setText(rStatus);
                        }
                        else
                        {
                            Toast.makeText(settings.this,"Please Enter Profile Info..",Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(settings.this,"Error:",Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if((requestCode == galleryPick) && (resultCode == RESULT_OK) && (data!=null))
        {
            Uri imageUri = data.getData();
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1,1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if(resultCode == RESULT_OK)
            {
                loadingBar.setTitle("Set Profile Image");
                loadingBar.setMessage("Please wait,while your profile image is updating");
                loadingBar.setCanceledOnTouchOutside(false);
                loadingBar.show();

                Uri resultUri = result.getUri();
                StorageReference filepath = UserProfileImgRef.child(currentUserId + ".jpg");
                filepath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        final String downloadUrl = uri.toString();
                        RootRef.child("USERS").child(currentUserId).child("image")
                                .setValue(downloadUrl)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful())
                                        {
                                            Toast.makeText(settings.this,"done",Toast.LENGTH_SHORT).show();
                                            loadingBar.dismiss();
                                        }
                                        else
                                        {
                                            loadingBar.dismiss();
                                            String msg = task.getException().toString();
                                            Toast.makeText(settings.this,"Error: "+msg,Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                });
                filepath.putFile(resultUri);
                /*
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(settings.this,"Profile Image Updated..",Toast.LENGTH_SHORT).show();
                            //uri can't found
                            final String downloadUrl = filepath.getMetadata().toString();

                            RootRef.child("USERS").child(currentUserId).child("image")
                                    .setValue(downloadUrl)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if(task.isSuccessful())
                                            {
                                                Toast.makeText(settings.this,"done",Toast.LENGTH_SHORT).show();
                                                loadingBar.dismiss();
                                            }
                                            else
                                            {
                                                loadingBar.dismiss();
                                                String msg = task.getException().toString();
                                                Toast.makeText(settings.this,"Error: "+msg,Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else
                        {
                            String msg = task.getException().toString();
                            Toast.makeText(settings.this,"Error :"+msg,Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });*/

            }
        }
    }

    private void UpdateSettings()
    {
        String setUserName = userName.getText().toString();
        String setStatus = status.getText().toString();

        if(TextUtils.isEmpty(setUserName))
        {
            Toast.makeText(settings.this,"Enter User Name...",Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setStatus))
        {
            Toast.makeText(settings.this,"Enter Status...",Toast.LENGTH_SHORT).show();
        }
        else {
            RootRef.child("USERS").child(currentUserId).child("uid").setValue(currentUserId);
            RootRef.child("USERS").child(currentUserId).child("name").setValue(setUserName);
            RootRef.child("USERS").child(currentUserId).child("status").setValue(setStatus)
            .addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful())
                    {
                        Intent home = new Intent(settings.this,HomeActivity.class);
                        home.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(home);
                        finish();
                        Toast.makeText(settings.this,"Profile Updated Successfuly...",Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        String msg = task.getException().toString();
                        Toast.makeText(settings.this,"Error "+msg,Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
}
