package com.example.eventsnapqr;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * fragment to show which events a user is associated with
 */
public class MyEventsFragment extends Fragment {
    private FirebaseFirestore db;
    private ArrayAdapter<String> organizeEventAdapter, attendEventAdapter;
    private ArrayList<String> organizeEventNames, attendEventNames;
    private ListView attendEventListView, organizeEventListView;
    private ImageView backButton;
    private List<String> attendEventId;
    private List<String> organizeEventId;

    /**
     * Setup actions to be taken upon view creation and when the views are interacted with
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return the final view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_my_events, container, false);

        attendEventListView = view.findViewById(R.id.attending_events_list);
        organizeEventListView = view.findViewById(R.id.organized_events_list);
        String androidId = Settings.Secure.getString(getActivity().getContentResolver(), Settings.Secure.ANDROID_ID);

        db = FirebaseFirestore.getInstance();

        loadOrganizedEvents(androidId);
        loadAttendingEvents(androidId);

        attendEventNames = new ArrayList<>();
        organizeEventNames = new ArrayList<>();

        attendEventId = new ArrayList<>();
        organizeEventId= new ArrayList<>();

        attendEventAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, attendEventNames);
        organizeEventAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, organizeEventNames);

        attendEventListView.setAdapter(attendEventAdapter);
        organizeEventListView.setAdapter(organizeEventAdapter);

        backButton = view.findViewById(R.id.button_back_button);
        backButton.setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(view);
            navController.navigate(R.id.action_myEventsFragment_to_BrowseEventFragment);
        });

        setListViewListeners(view);

        return view;
    }

    /**
     * populate each of the list views
     * @param view
     */
    private void setListViewListeners(View view) {
        attendEventListView.setOnItemClickListener((parent, view1, position, id) -> {
            String eventId = attendEventId.get(position);
            NavController navController = Navigation.findNavController(view);
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);
            navController.navigate(R.id.action_myEventsFragment_to_eventDetailFragment, bundle);
        });

        organizeEventListView.setOnItemClickListener((parent, view1, position, id) -> {
            String eventId = organizeEventId.get(position);
            NavController navController = Navigation.findNavController(view);
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);
            navController.navigate(R.id.action_myEventsFragment_to_yourEventFragment, bundle);
        });
    }

    /**
     * fetch all the events that the given user is organizing
     * @param userId
     */
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
                                            organizeEventId.add(eventId);
                                            Log.d("EVENTNAME",eventName);
                                            Log.d("EVENTID", eventId);
                                            organizeEventAdapter.notifyDataSetChanged();
                                        }
                                    })
                                    .addOnFailureListener(e -> Log.e("Error", "Error getting event details: ", e));
                        }
                    } else {
                        Log.e("Error", "Error getting organized events: ", task.getException());
                        Toast.makeText(getContext(), "Error loading attending events", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * fetch all the events that the given user is attending
     * @param userId
     */
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
                                            attendEventId.add(eventId);
                                            Log.d("EVENTNAME",eventName);
                                            Log.d("EVENTID", eventId);
                                            attendEventAdapter.notifyDataSetChanged();
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
