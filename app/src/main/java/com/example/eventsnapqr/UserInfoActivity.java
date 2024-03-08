package com.example.eventsnapqr;

import static java.security.AccessController.getContext;

import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
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
    private TextView userName;
    private TextView email;
    private TextView phoneNumber;
    private TextView homepage;

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        buttonBackButton = findViewById(R.id.back_button);
        buttonAddImage = findViewById(R.id.upload_profile_button);
        buttonRemoveImage = findViewById(R.id.delete_profile_button);
        profilePictureImage = findViewById(R.id.iv_profile_pic);
        userName = findViewById(R.id.name_context);
        email = findViewById(R.id.email_context);
        phoneNumber = findViewById(R.id.phone_context);
        homepage = findViewById(R.id.homepage_context);

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
                    if (user.getProfilePicture() != null) {
                        Glide.with(getBaseContext())
                                .load(Uri.parse(user.getProfilePicture()))
                                .dontAnimate()
                                .into(profilePictureImage);
                    }
                    userName.setText(user.getName());
                    email.setText(user.getEmail());
                    phoneNumber.setText(user.getPhoneNumber());
                    homepage.setText(user.getHomepage());
                }
                if (profilePictureURI == null || profilePictureURI.isEmpty()){
                    userName.setText(user.getName());
                    Bitmap initialsImageBitmap = user.generateInitialsImage(userName.getText().toString());
                    profilePictureImage.setImageBitmap(initialsImageBitmap);
                    email.setText(user.getEmail());
                    phoneNumber.setText(user.getPhoneNumber());
                    homepage.setText(user.getHomepage());

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
                                FirebaseController.getInstance().getUser(androidID, new FirebaseController.OnUserRetrievedListener() {
                                    @Override
                                    public void onUserRetrieved(User user) {
                                        if (user != null) {
                                            user.setProfilePicture(profilePictureURI);
                                            FirebaseController.getInstance().addUser(user);
                                        }
                                    }
                                });
                                Glide.with(this)
                                        .load(uri1)
                                        .dontAnimate()
                                        .into(profilePictureImage);
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