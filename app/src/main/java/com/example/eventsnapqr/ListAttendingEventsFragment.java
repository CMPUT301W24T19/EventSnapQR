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
import android.widget.ProgressBar;
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
 * fragment for attendees to browse any events that they are currently signed up for
 */
public class ListAttendingEventsFragment extends Fragment {
    private ListView eventListView; // list of events
    private List<String> attendEventIds; // list of event ids
    private ArrayAdapter<String> attendEventAdapter;
    private ArrayList<String>  attendEventNames;
    private FirebaseFirestore db; // database instance
    private String userId;
    // TODO add search for browse events

    /**
     * fetch all the events that the given user is attending
     * @param userId
     */
    private void loadAttendingEvents(String userId, ProgressBar loadingProgressBar) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(userId).collection("promisedEvents")
                .get()
                .addOnCompleteListener(task -> {
                    loadingProgressBar.setVisibility(View.GONE);
                    if (task.isSuccessful()) {
                        attendEventNames.clear(); // Clear existing items
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId();
                            db.collection("events").document(eventId).get()
                                    .addOnSuccessListener(eventDocument -> {
                                        String eventName = eventDocument.getString("eventName");
                                        if (eventName != null) {
                                            attendEventNames.add(eventName);
                                            attendEventIds.add(eventId);
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

        ProgressBar loadingProgressBar = view.findViewById(R.id.loadingProgressBar);
        eventListView = view.findViewById(R.id.events);
        attendEventNames = new ArrayList<>();
        attendEventIds = new ArrayList<>();
        attendEventAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, attendEventNames);
        eventListView.setAdapter(attendEventAdapter);
        userId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        db = FirebaseFirestore.getInstance();
        loadAttendingEvents(userId, loadingProgressBar);

        eventListView.setOnItemClickListener((parent, view1, position, id) -> {
            String eventId = attendEventIds.get(position);
            BrowseEventsActivity activity = (BrowseEventsActivity) requireActivity();
            activity.switchToFullscreenDetails(eventId, false);
        });

        return view;
    }
}
