package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class NotificationsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notifications);
        String notification = getIntent().getStringExtra("notification").toString();
        TextView textViewNotification = findViewById(R.id.notification_data);
        textViewNotification.setText(notification);
    }
}