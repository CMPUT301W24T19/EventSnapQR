package com.example.eventsnapqr;

import static java.security.AccessController.getContext;

import android.content.ContentResolver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class UserInfoActivity extends AppCompatActivity {
    ImageView buttonBackButton;
    ImageView buttonAddImage;
    ImageView buttonRemoveImage;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    String androidID;
    ImageView profilePictureImage;
    String profilePictureURI;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info);
        buttonBackButton = findViewById(R.id.back_button);
        buttonAddImage = findViewById(R.id.upload_profile_button);
        buttonRemoveImage = findViewById(R.id.delete_profile_button);
        profilePictureImage = findViewById(R.id.iv_profile_pic);
        buttonBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ContentResolver contentResolver = getBaseContext().getContentResolver();
        androidID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

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

            }
        });
    }
}