package com.example.eventsnapqr;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * fragment where a user can view another users profile when view event details (organizer)
 * or when managing an event (attendees)
 */
public class ViewUserProfileActivity extends AppCompatActivity {

    private ImageView imageViewProfilePic, backButton;
    private TextView textViewName, textViewEmail, textViewPhone, textViewHomepage;

    /**
     * What should be executed when the fragment is created
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_user_profile);

        textViewName = findViewById(R.id.editTextName);
        textViewEmail = findViewById(R.id.editTextEmail);
        textViewPhone = findViewById(R.id.editTextPhoneNumber);
        textViewHomepage = findViewById(R.id.editTextHomepage);
        imageViewProfilePic = findViewById(R.id.iv_profile_pic);
        backButton = findViewById(R.id.back_button);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        String attendeeId = getIntent().getStringExtra("userId");

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        FirebaseController.getInstance().getUser(attendeeId, new FirebaseController.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(User user) {
                if (user != null) {
                    String name = user.getName();
                    String email = user.getEmail();
                    String phone = user.getPhoneNumber();
                    String homepage = user.getHomepage();
                    String profilePicUri = user.getProfilePicture();

                    if (name == null || name.isEmpty()) {
                        name = "N/A";
                    }

                    if (email == null || email.isEmpty()) {
                        email = "N/A";
                    }
                    if (phone == null || phone.isEmpty()) {
                        phone = "N/A";
                    }
                    if (homepage == null || homepage.isEmpty()) {
                        homepage = "N/A";
                    }

                    if (profilePicUri != null && !profilePicUri.isEmpty()) {
                        Glide.with(ViewUserProfileActivity.this)
                                .load(Uri.parse(profilePicUri))
                                .into(imageViewProfilePic);
                    } else {
                        Bitmap initialsImageBitmap = user.generateInitialsImage(name);
                        imageViewProfilePic.setImageBitmap(initialsImageBitmap);
                    }

                    textViewName.setText(name);
                    textViewEmail.setText(email);
                    textViewPhone.setText(phone);
                    textViewHomepage.setText(homepage);
                }
            }
        });
    }
}
