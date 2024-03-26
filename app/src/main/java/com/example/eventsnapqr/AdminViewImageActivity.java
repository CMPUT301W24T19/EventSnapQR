package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

/**
 * activity to view an image in full when in admin mode
 */
public class AdminViewImageActivity extends AppCompatActivity {
    private ImageView backButton; // Changed to ImageView
    private ImageView eventPoster;

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_poster);

        // hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        backButton = findViewById(R.id.button_back_button); // Changed to ImageView
        eventPoster = findViewById(R.id.event_poster);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });



        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            Object uriObject = extra.get("uri");
            if (uriObject != null) {
                String uriString = uriObject.toString();
                Glide.with(eventPoster)
                        .load(uriString)
                        .placeholder(R.drawable.place_holder_img)
                        .dontAnimate()
                        .into(eventPoster);
            }
        }

    }
}
