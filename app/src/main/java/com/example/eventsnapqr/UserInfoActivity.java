package com.example.eventsnapqr;
import android.Manifest;

import static java.security.AccessController.getContext;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
//import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;


/**
 * activity where the user can view and edit their information. the geolocation and
 * notification switches have no yet been implemented
 */
public class UserInfoActivity extends AppCompatActivity {
    private ImageView buttonBackButton;
    private ImageView buttonAddImage;
    private ImageView buttonRemoveImage;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private String androidID;
    private ImageView profilePictureImage;
    private String profilePictureURI;
    private EditText userName;
    private EditText email;
    private EditText phoneNumber;
    private EditText homepage;
    private Button saveButton;
    private boolean editMode = false;
    private ImageView editButton;
    private StorageTask<UploadTask.TaskSnapshot> uploadSuccess;
    private Switch locationSwitch;

    private final int PERMISSION_REQUEST_CODE = 100;
    String[] permissions = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_REQUEST_CODE){
            PermissionClient.getInstance(UserInfoActivity.this).permissionResult(UserInfoActivity.this ,permissions, grantResults,requestCode);
        }
    }

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_user_info);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        buttonBackButton = findViewById(R.id.back_button);
        buttonAddImage = findViewById(R.id.upload_profile_button);
        buttonRemoveImage = findViewById(R.id.delete_profile_button);
        profilePictureImage = findViewById(R.id.iv_profile_pic);
        userName = findViewById(R.id.name_context);
        email = findViewById(R.id.email_context);
        phoneNumber = findViewById(R.id.phone_context);
        homepage = findViewById(R.id.homepage_context);
        saveButton = findViewById(R.id.button_save_button);
        editButton = findViewById(R.id.button_edit_profile_button);
        locationSwitch = findViewById(R.id.switch_geolocation);


        locationSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!PermissionClient.getInstance(UserInfoActivity.this).checkPermission(permissions)){
                    PermissionClient.getInstance(UserInfoActivity.this).askPermissions(UserInfoActivity.this,permissions, PERMISSION_REQUEST_CODE);
                }
                else{
                    Toast.makeText(UserInfoActivity.this, "Permission already granted", Toast.LENGTH_LONG).show();
                }
            }
        });

        userName.setEnabled(editMode);
        email.setEnabled(editMode);
        phoneNumber.setEnabled(editMode);
        homepage.setEnabled(editMode);

        saveButton.setVisibility(View.INVISIBLE);
        buttonBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle extra = getIntent().getExtras();
        androidID = extra.get("androidId").toString();

        FirebaseController.getInstance().getUser(androidID, new FirebaseController.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(User user) {
                if (user != null) {
                    if (user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                        Glide.with(getBaseContext())
                                .load(Uri.parse(user.getProfilePicture()))
                                .into(profilePictureImage);
                    }
                    else if (user.getProfilePicture() == null || user.getProfilePicture().isEmpty()) {
                        Bitmap initialsImageBitmap = user.generateInitialsImage(user.getName().toString());
                        profilePictureImage.setImageBitmap(initialsImageBitmap);
                    }

                    userName.setText(user.getName());
                    Log.d("TAG", "User name: " + user.getName());
                    email.setText(user.getEmail());
                    Log.d("TAG", "User email: " + user.getEmail());
                    phoneNumber.setText(user.getPhoneNumber());
                    Log.d("TAG", "User phone number: " + user.getPhoneNumber());
                    homepage.setText(user.getHomepage());
                    Log.d("TAG", "User homepage: " + user.getHomepage());
                }
            }
        });

        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        StorageReference userRef = storageRef.child("users/" + androidID);  // specifies the path on the cloud storage
                        userRef.putFile(uri).addOnSuccessListener(taskSnapshot -> {
                            userRef.getDownloadUrl().addOnSuccessListener(uri1 -> {
                                profilePictureURI = String.valueOf(uri1);
                                Glide.with(this)
                                        .load(uri1)
                                        .dontAnimate()
                                        .into(profilePictureImage);
                                FirebaseController.getInstance().getUser(androidID, new FirebaseController.OnUserRetrievedListener() {
                                    @Override
                                    public void onUserRetrieved(User user) {
                                        user.setProfilePicture(profilePictureURI);
                                        FirebaseController.getInstance().addUser(user);
                                    }
                                });
                            });
                        });  // puts the file into the referenced path
                    } else {
                        Log.d("TAG", "No media selected");
                    }
                });
        buttonAddImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // "opens up" activity
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseController.getInstance().getUser(androidID, new FirebaseController.OnUserRetrievedListener() {
                    @Override
                    public void onUserRetrieved(User user) {
                        user.setName(userName.getText().toString());
                        user.setEmail(email.getText().toString());
                        user.setPhoneNumber(phoneNumber.getText().toString());
                        user.setHomepage(homepage.getText().toString());
                        FirebaseController.getInstance().addUser(user);
                        if (user.getProfilePicture() == null) {
                            Bitmap initialsImageBitmap = user.generateInitialsImage(user.getName().toString());
                            profilePictureImage.setImageBitmap(initialsImageBitmap);
                        }
                        Toast.makeText(UserInfoActivity.this, "Information successfully updated!", Toast.LENGTH_SHORT).show();
                        editMode = false;
                        saveButton.setVisibility(View.INVISIBLE);
                    }
                });
            }
        });
        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editMode = !editMode;
                userName.setEnabled(editMode);
                email.setEnabled(editMode);
                phoneNumber.setEnabled(editMode);
                homepage.setEnabled(editMode);
                if (editMode) {
                    saveButton.setVisibility(View.VISIBLE);
                }
                else {
                    saveButton.setVisibility(View.INVISIBLE);
                }
            }
        });
        buttonRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseController.getInstance().getUser(androidID, new FirebaseController.OnUserRetrievedListener() {
                    @Override
                    public void onUserRetrieved(User user) {
                        if (user != null) {
                            if (user.getProfilePicture() != null) {
                                String[] uriPaths = Uri.parse(user.getProfilePicture()).getPath().split("/");
                                String storagePath = uriPaths[uriPaths.length - 2] + "/" + uriPaths[uriPaths.length - 1];
                                Log.d("TAG", "Path: " + storagePath);
                                storageRef.child(storagePath).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Log.d("TAG", "Picture successfully deleted");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("TAG", "Picture not deleted");
                                    }
                                });
                                user.setProfilePicture(null);
                                FirebaseController.getInstance().addUser(user);

                                if (profilePictureURI == null || profilePictureURI.isEmpty()) {
                                    userName.setText(user.getName());
                                    Bitmap initialsImageBitmap = user.generateInitialsImage(userName.getText().toString());
                                    profilePictureImage.setImageBitmap(initialsImageBitmap);
                                }
                            }
                        }
                    }
                });
            }
        });
    }
}