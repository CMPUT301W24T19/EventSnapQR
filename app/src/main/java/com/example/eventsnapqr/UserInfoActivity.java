package com.example.eventsnapqr;
import android.Manifest;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
//import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.List;


/**
 * activity where the user can view and edit their information. the geolocation and
 * notification switches have no yet been implemented
 */
public class UserInfoActivity extends AppCompatActivity {
    private ImageView buttonBackButton, buttonAddImage, buttonRemoveImage, profilePictureImage, editButton;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private String androidID;
    private String profilePictureURI;
    private TextInputEditText userName, email, phoneNumber, homepage;
    private TextInputLayout userNameLayout, emailLayout, numberLayout,homepageLayout;
    private ExtendedFloatingActionButton saveButton;
    private boolean editMode = false;
    private StorageTask<UploadTask.TaskSnapshot> uploadSuccess;
    private MaterialButton locationButton;
    private Button notificationButton;
    private boolean showSwitches;
    private final int PERMISSION_REQUEST_CODE = 100;
    String[] locationPermissions = {Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION};
    String[] notificationPermissions = {Manifest.permission.POST_NOTIFICATIONS};
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
        userName = findViewById(R.id.editTextUserName);
        userNameLayout = findViewById(R.id.textInputUserName);
        email = findViewById(R.id.editTextEmail);
        emailLayout = findViewById(R.id.textInputEmail);
        phoneNumber = findViewById(R.id.editTextPhoneNumber);
        numberLayout = findViewById(R.id.inputTextPhoneNumber);
        homepage = findViewById(R.id.editTextHomepage);
        homepageLayout = findViewById(R.id.textInputHomepage);
        saveButton = findViewById(R.id.saveButton);
        editButton = findViewById(R.id.button_edit_profile_button);
        locationButton = findViewById(R.id.locationButton);
        notificationButton = findViewById(R.id.notificationButton);


        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                intent.putExtra(Settings.EXTRA_APP_PACKAGE, getPackageName());
                startActivity(intent);
            }
        });


        locationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(intent);
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
        showSwitches = extra.getBoolean("showSwitches");

        if (!showSwitches) {
            locationButton.setVisibility(View.INVISIBLE);
            notificationButton.setVisibility(View.INVISIBLE);
        }

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

                    userName.setText(user.getName().trim());
                    if (user.getEmail() != null) {
                        email.setText(user.getEmail().trim());
                    }
                    if (user.getPhoneNumber() != null) {
                        phoneNumber.setText(user.getPhoneNumber().trim());
                    }
                    if (user.getHomepage() != null) {
                        homepage.setText(user.getHomepage().trim());
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
                String name = userName.getText().toString().trim();
                String userEmail = email.getText().toString().trim();
                String userPhoneNumber = phoneNumber.getText().toString().trim();
                String userHomepage = homepage.getText().toString().trim();

                List<String> errorMessages = new ArrayList<>();

                if (name.isEmpty()) {
                    errorMessages.add("Name cannot be empty");
                }

                // validate phone number if not empty
                if (!userPhoneNumber.isEmpty() && !isValidPhoneNumber(userPhoneNumber)) {
                    errorMessages.add("Invalid phone number format");
                }

                // validate email format if not empty
                if (!userEmail.isEmpty() && !isValidEmail(userEmail)) {
                    errorMessages.add("Invalid email format");
                }

                // validate homepage if not empty
                if (!userHomepage.isEmpty() && !isValidHomepage(userHomepage)) {
                    errorMessages.add("Invalid homepage format");
                }

                // display the errors
                if (!errorMessages.isEmpty()) {
                    for (String errorMessage : errorMessages) {
                        if (errorMessage.contains("Name cannot be empty")) {
                            userName.setError(errorMessage);
                        } else if (errorMessage.contains("Invalid phone number format")) {
                            phoneNumber.setError(errorMessage);
                        } else if (errorMessage.contains("Invalid homepage format")) {
                            homepage.setError(errorMessage);
                        } else if (errorMessage.contains("Invalid email format")) {
                            email.setError(errorMessage);
                        }
                    }
                    return;
                }

                FirebaseController.getInstance().getUser(androidID, new FirebaseController.OnUserRetrievedListener() {
                    @Override
                    public void onUserRetrieved(User user) {
                        user.setName(name);
                        user.setEmail(userEmail);
                        user.setPhoneNumber(userPhoneNumber);
                        user.setHomepage(userHomepage);
                        FirebaseController.getInstance().addUser(user);

                        Toast.makeText(UserInfoActivity.this, "Information successfully updated!", Toast.LENGTH_SHORT).show();

                        saveButtonLayoutSwitch();
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
                    userNameLayout.setCounterEnabled(true);
                    emailLayout.setCounterEnabled(true);
                    numberLayout.setCounterEnabled(true);
                    homepageLayout.setCounterEnabled(true);

                    userNameLayout.setHelperText("* Required");
                    numberLayout.setHelperText("Format: ***-***-****");
                }
                else {
                    saveButton.setVisibility(View.INVISIBLE);
                    userNameLayout.setCounterEnabled(false);
                    emailLayout.setCounterEnabled(false);
                    numberLayout.setCounterEnabled(false);
                    homepageLayout.setCounterEnabled(false);

                    userNameLayout.setHelperTextEnabled(false);
                    numberLayout.setHelperTextEnabled(false);
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

    /**
     * after the save button is pressed these layout properties must be flipped
     */
    private void saveButtonLayoutSwitch() {
        userName.setError(null);
        email.setError(null);
        homepage.setError(null);
        phoneNumber.setError(null);

        userNameLayout.setCounterEnabled(false);
        emailLayout.setCounterEnabled(false);
        numberLayout.setCounterEnabled(false);
        homepageLayout.setCounterEnabled(false);

        userNameLayout.setHelperTextEnabled(false);
        emailLayout.setHelperTextEnabled(false);
        numberLayout.setHelperTextEnabled(false);
        homepageLayout.setCounterEnabled(false);

        editMode = false;
        saveButton.setVisibility(View.INVISIBLE);
        userName.setEnabled(editMode);
        email.setEnabled(editMode);
        phoneNumber.setEnabled(editMode);
        homepage.setEnabled(editMode);
    }

    /**
     * determines if a given string is formatted as a phone number
     * @param phoneNumber
     * @return
     */
    private boolean isValidPhoneNumber(String phoneNumber) {
        if (!phoneNumber.matches("\\d{3}-\\d{3}-\\d{4}")) {
            return false;
        }

        String digitsOnly = phoneNumber.replaceAll("-", "");

        if (digitsOnly.length() != 10) {
            return false;
        }

        return true;
    }

    /**
     * determines if a given string resembles an email address
     * @param email string to be checked
     * @return true if the string resembles an email address, otherwise false
     */
    private boolean isValidEmail(String email) {
        String emailPattern = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailPattern);
    }

    /**
     * determines if a given string resembles a homepage
     * @param homepage string to be checked
     * @return true if the string resembles a homepage, otherwise false
     */
    private boolean isValidHomepage(String homepage) {
        String homepagePattern = ".*\\..*";
        return homepage.matches(homepagePattern);
    }
}