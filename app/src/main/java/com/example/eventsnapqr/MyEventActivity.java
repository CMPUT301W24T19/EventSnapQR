package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyEventActivity extends AppCompatActivity {

    private ListView attend_eventListView, orgnize_eventListView;
    private ArrayAdapter<String> eventAdapter;
    private List<String> eventNames;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_event);
        attend_eventListView = findViewById(R.id.attending_events);
        orgnize_eventListView = findViewById(R.id.organized_events);

        eventNames = new ArrayList<>();
        eventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, eventNames);
        attend_eventListView.setAdapter(eventAdapter);
        orgnize_eventListView.setAdapter(eventAdapter);

        db = FirebaseFirestore.getInstance();
        loadEvents();

        findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void loadEvents() {
        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventNames.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String eventName = document.getId();
                            eventNames.add(eventName);
                        }
                        eventAdapter.notifyDataSetChanged();
                    }
                    else {} // error handling?
                });
    }

}