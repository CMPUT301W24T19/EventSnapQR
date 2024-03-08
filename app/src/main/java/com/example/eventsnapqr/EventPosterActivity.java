package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class EventPosterActivity extends AppCompatActivity {
    FloatingActionButton backButton;
    ImageView eventPoster;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_poster);
        backButton = findViewById(R.id.button_back_button);
        eventPoster = findViewById(R.id.event_poster);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle extra = getIntent().getExtras();
        if (extra != null) {
            Glide.with(eventPoster)
                    .load(extra.get("uri"))
                    .placeholder(R.drawable.place_holder_img)
                    .dontAnimate()
                    .into(eventPoster);
        }
    }
}