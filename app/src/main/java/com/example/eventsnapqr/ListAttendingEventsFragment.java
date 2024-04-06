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

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * fragment for attendees to browse any events that they are currently signed up for
 */
public class ListAttendingEventsFragment extends Fragment {
    private ListView eventListView;
    private EventAdapter eventAdapter;
    private List<Event> attendingEvents;
    private FirebaseFirestore db;
    private String userId;


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
        attendingEvents = new ArrayList<>();
        eventAdapter = new EventAdapter(requireContext(), attendingEvents);
        eventListView.setAdapter(eventAdapter);
        userId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        db = FirebaseFirestore.getInstance();
        eventListView.setVisibility(View.INVISIBLE);
        loadAttendingEvents(userId, loadingProgressBar);

        eventListView.setOnItemClickListener((parent, view1, position, id) -> {
            String eventId = attendingEvents.get(position).getEventID();
            BrowseEventsActivity activity = (BrowseEventsActivity) requireActivity();
            activity.switchToFullscreenDetails(eventId, false);
        });

        return view;
    }

    /**
     * fetch all the events that the given user is attending
     * @param userId
     */
    private void loadAttendingEvents(String userId, ProgressBar loadingProgressBar) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        db.collection("users").document(userId).collection("promisedEvents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        attendingEvents.clear();
                        int[] i = {0};
                        QuerySnapshot documents = task.getResult();
                        if (documents.size() != 0) {
                            for (QueryDocumentSnapshot document : documents) {
                                String eventId = document.getId();
                                db.collection("events").document(eventId).get()
                                        .addOnSuccessListener(eventDocument -> {
                                            if (eventDocument.exists()) {
                                                Long maxAttendeesLong = document.getLong("maxAttendees");
                                                int maxAttendees = (maxAttendeesLong != null) ? maxAttendeesLong.intValue() : 0;
                                                String eventName = eventDocument.getString("eventName");
                                                String organizerId = eventDocument.getString("organizerID");
                                                String posterURI = eventDocument.getString("posterURI");

                                                FirebaseController.getInstance().getUser(organizerId, user -> {
                                                    if (user != null) {
                                                        Event event = new Event(
                                                                user,
                                                                eventName,
                                                                eventDocument.getString("description"),
                                                                posterURI,
                                                                maxAttendees,
                                                                eventId,
                                                                eventDocument.getDate("eventStartDateTime"),
                                                                eventDocument.getDate("eventEndDateTime"),
                                                                eventDocument.getString("address"),
                                                                eventDocument.getBoolean("active")
                                                        );
                                                        attendingEvents.add(event);
                                                        if (i[0] == documents.size() - 1) {
                                                            attendingEvents.sort(new Comparator<Event>() {
                                                                @Override
                                                                public int compare(Event o1, Event o2) {
                                                                    String event1 = o1.getEventName();
                                                                    event1 = event1.toLowerCase();
                                                                    String event2 = o2.getEventName();
                                                                    event2 = event2.toLowerCase();
                                                                    return event1.compareTo(event2);
                                                                }
                                                            });
                                                            eventListView.setVisibility(View.VISIBLE);
                                                            loadingProgressBar.setVisibility(View.GONE);
                                                            eventAdapter.notifyDataSetChanged();
                                                        }
                                                    } else {
                                                        Toast.makeText(requireContext(), "Organizer not found for event: " + eventId, Toast.LENGTH_SHORT).show();
                                                    }
                                                    i[0]++;
                                                });
                                            } else {
                                                Log.e("Error", "Event document doesn't exist");
                                                i[0]++;
                                            }
                                        })
                                        .addOnFailureListener(e -> Log.e("Error", "Error getting event details: ", e));
                            }
                        } else {
                            eventListView.setVisibility(View.VISIBLE);
                            loadingProgressBar.setVisibility(View.GONE);
                            eventAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Log.e("Error", "Error getting attending events: ", task.getException());
                        Toast.makeText(getContext(), "Error loading attending events", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
