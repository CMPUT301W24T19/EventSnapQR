package com.example.eventsnapqr;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.ListView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class MyEventActivity extends AppCompatActivity {

    private ListView attendEventListView, organizeEventListView;
    private ArrayList<Event> attendingList, organizedList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_my_events);
        attendEventListView = findViewById(R.id.attending_events_list);
        organizeEventListView = findViewById(R.id.organized_events_list);
        attendingList = new ArrayList<>();
        organizedList = new ArrayList<>();


        EventArrayAdapter attendEventArrayAdapter = new EventArrayAdapter(getBaseContext(), attendingList);
        EventArrayAdapter organizeEventArrayAdapter = new EventArrayAdapter(getBaseContext(), organizedList);
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        organizeEventListView.setAdapter(organizeEventArrayAdapter);
        attendEventListView.setAdapter(attendEventArrayAdapter);

        FirebaseFirestore.getInstance().collection(androidId).document(androidId).collection("organizedEvents").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot doc: value) {
                    
                }
            }
        });
        // goToMyEventsFragment();

    }

  public void goToMyEventsFragment() {
        //NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_2);
       // navController.navigate(R.id.myEventsFragment);
    }

}