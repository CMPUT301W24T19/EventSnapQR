package com.example.eventsnapqr;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;


public class MyEventActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private ArrayAdapter<String> organizeEventAdapter, attendEventAdapter;
    private ArrayList<String> organizeEventNames, attendEventNames;
    private ListView attendEventListView, organizeEventListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_my_events); // Ensure you have a corresponding layout

        attendEventListView = findViewById(R.id.attending_events_list);
        organizeEventListView = findViewById(R.id.organized_events_list);
        String androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        attendEventNames = new ArrayList<>();
        organizeEventNames = new ArrayList<>();

        attendEventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendEventNames);
        organizeEventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, organizeEventNames);


        attendEventListView.setAdapter(attendEventAdapter);
        organizeEventListView.setAdapter(organizeEventAdapter);

        db = FirebaseFirestore.getInstance();

        // backButton functionality has to be adjusted for an activity
        // Back button action here depends on how you want to navigate back

        loadOrganizedEvents(androidId);
        loadAttendingEvents(androidId);
    }

    private void loadOrganizedEvents(String userId) {
        db.collection("users").document(userId).collection("organizedEvents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        organizeEventNames.clear(); // Clear existing items
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId();
                            db.collection("events").document(eventId).get()
                                    .addOnSuccessListener(eventDocument -> {
                                        String eventName = eventDocument.getString("eventName");
                                        if (eventName != null) {
                                            organizeEventNames.add(eventName);
                                            organizeEventAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("Error", "Error getting event details: ", e));
                        }
                    } else {
                        Log.e("Error", "Error getting organized events: ", task.getException());
                        Toast.makeText(MyEventActivity.this, "Error loading organized events", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadAttendingEvents(String userId) {
        db.collection("users").document(userId).collection("promisedEvents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        attendEventNames.clear(); // Clear existing items
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId();
                            db.collection("events").document(eventId).get()
                                    .addOnSuccessListener(eventDocument -> {
                                        String eventName = eventDocument.getString("eventName");
                                        if (eventName != null) {
                                            attendEventNames.add(eventName);
                                            attendEventAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("Error", "Error getting event details: ", e));
                        }
                    } else {
                        Log.e("Error", "Error getting attending events: ", task.getException());
                        Toast.makeText(MyEventActivity.this, "Error loading attending events", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
