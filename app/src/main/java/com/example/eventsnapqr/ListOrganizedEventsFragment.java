package com.example.eventsnapqr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * fragment for organzers to browse any events that they have organized
 */
public class ListOrganizedEventsFragment extends Fragment {
    private ListView eventListView; // list of events
    private List<String> organizedEventIds; // list of event ids
    private ArrayAdapter<String> organizedEventAdapter;
    private ArrayList<String>  organizedEventNames;
    private FirebaseFirestore db; // database instance
    private String userId;
    // TODO add search for browse events

    /**
     * fetch all the events that the given user is organizing
     * @param userId
     */
    private void loadOrganizedEvents(String userId) {
        db.collection("users").document(userId).collection("organizedEvents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        organizedEventNames.clear(); // Clear existing items
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId();
                            db.collection("events").document(eventId).get()
                                    .addOnSuccessListener(eventDocument -> {
                                        String eventName = eventDocument.getString("eventName");
                                        if (eventName != null) {
                                            organizedEventNames.add(eventName);
                                            organizedEventIds.add(eventId);
                                            Log.d("EVENTNAME",eventName);
                                            Log.d("EVENTID", eventId);
                                            organizedEventAdapter.notifyDataSetChanged();
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
     * Setup actions to be taken upon view creation and when the views are interacted with
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the resulting view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_browse_events, container, false);

        eventListView = view.findViewById(R.id.events);
        organizedEventNames = new ArrayList<>();
        organizedEventIds = new ArrayList<>();
        organizedEventAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, organizedEventNames);
        eventListView.setAdapter(organizedEventAdapter);
        userId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        db = FirebaseFirestore.getInstance();
        loadOrganizedEvents(userId);

        eventListView.setOnItemClickListener((parent, view1, position, id) -> {
            String eventId = organizedEventIds.get(position);
            BrowseEventsActivity activity = (BrowseEventsActivity) requireActivity();
            activity.switchToFullscreenManage(eventId);
        });

        return view;
    }
}
