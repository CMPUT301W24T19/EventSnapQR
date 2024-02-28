package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class EventDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);
        String name = getIntent().getStringExtra("event_name");
        TextView tvEventAnnouncement = findViewById(R.id.event_announcement);
        tvEventAnnouncement.setText(name);
    }
}