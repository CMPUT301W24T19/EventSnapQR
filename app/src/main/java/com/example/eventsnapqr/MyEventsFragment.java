package com.example.eventsnapqr;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * fragment for a user to browse all the events they are currently signed up to attend, and
 * the events they have organized. if an attending event is pressed, you are brought to the
 * event details page. if an organized event is pressed you are brought to "your event fragment"
 */
public class MyEventsFragment extends Fragment {
    private FirebaseFirestore db;
    private ArrayAdapter<String> organize_eventAdapter;
    private ArrayAdapter<String> attend_eventAdapter;
    private ArrayList<String> organize_eventNames;
    private ArrayList<String> attend_eventNames;
    FloatingActionButton backButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);
        ListView attend_eventListView = view.findViewById(R.id.attending_events_list);
        ListView organize_eventListView = view.findViewById(R.id.organized_events_list);
        String androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        attend_eventNames = new ArrayList<>();
        organize_eventNames = new ArrayList<>();
        attend_eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, attend_eventNames);
        organize_eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, organize_eventNames);
        attend_eventListView.setAdapter(attend_eventAdapter);
        organize_eventListView.setAdapter(organize_eventAdapter);
        backButton = view.findViewById(R.id.button_back_button);

        db = FirebaseFirestore.getInstance();

        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_myEventActivity_to_BrowseEventFragment);
        });

        loadOrganizedEvents(androidId);
        loadAttendingEvents(androidId);

        return view;
    }

    private void loadOrganizedEvents(String userId) {
        db.collection("users").document(userId).collection("organizedEvents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId();
                            db.collection("events").document(eventId).get()
                                    .addOnSuccessListener(eventDocument -> {
                                        String eventName = eventDocument.getString("eventName");
                                        if (eventName != null) {
                                            organize_eventNames.add(eventName);
                                            organize_eventAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("Error", "Error getting event details: ", e));
                        }
                    } else {
                        Log.e("Error", "Error getting organized events: ", task.getException());
                        Toast.makeText(getContext(), "Error loading organized events", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadAttendingEvents(String userId) {
        db.collection("users").document(userId).collection("promisedEvents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId(); // Assuming the document ID is the event ID
                            db.collection("events").document(eventId).get()
                                    .addOnSuccessListener(eventDocument -> {
                                        String eventName = eventDocument.getString("eventName");
                                        if (eventName != null) {
                                            attend_eventNames.add(eventName);
                                            attend_eventAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("Error", "Error getting event details: ", e));
                        }
                    } else {
                        Log.e("Error", "Error getting attending events: ", task.getException());
                        Toast.makeText(getContext(), "Error loading attending events", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
