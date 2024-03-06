package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

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
        setContentView(R.layout.activity_my_event_main);
        // goToMyEventsFragment();

    }

  public void goToMyEventsFragment() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_2);
        navController.navigate(R.id.myEventsFragment);
    }

}