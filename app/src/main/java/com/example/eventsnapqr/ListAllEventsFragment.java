package com.example.eventsnapqr;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * fragment for attendees to browse any events that are are currently posted. comes from the main
 * page fragment and can lead to either an events detail page or a users current events page.
 */
public class ListAllEventsFragment extends Fragment {
    private ListView eventListView; // list of events
    private EventAdapter eventAdapter;
    private List<Event> events;
    private FirebaseFirestore db; // database instance
    private TextView noEventsText; // empty list message

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
        events = new ArrayList<>();
        eventAdapter = new EventAdapter(requireContext(), events);
        eventListView.setAdapter(eventAdapter);
        db = FirebaseFirestore.getInstance();
        noEventsText = view.findViewById(R.id.noEventsTextView);

        ProgressBar loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

        eventListView.setVisibility(View.INVISIBLE);
        loadEvents(loadingProgressBar);

        eventListView.setOnItemClickListener((parent, view1, position, id) -> {
            String eventId = events.get(position).getEventID();
            BrowseEventsActivity activity = (BrowseEventsActivity) requireActivity();
            activity.switchToFullscreenDetails(eventId, false);
        });

        return view;
    }

    /**
     * retrieve any events that are currently in the database.
     */
    private void loadEvents(ProgressBar loadingProgressBar) {
        loadingProgressBar.setVisibility(View.VISIBLE);
        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        events.clear();
                        int[] i = {0};
                        QuerySnapshot documents = task.getResult();
                        if (documents.size() > 0) {
                            noEventsText.setVisibility(View.INVISIBLE);
                            for (DocumentSnapshot document : documents) {
                                if (Boolean.TRUE.equals(document.getBoolean("active"))) {
                                    Long maxAttendeesLong = document.getLong("maxAttendees");
                                    int maxAttendees = (maxAttendeesLong != null) ? maxAttendeesLong.intValue() : 0;
                                    String organizerId = document.getString("organizerID");
                                    FirebaseController.getInstance().getUser(organizerId, user -> {
                                        if (user != null) {
                                            Date startDateTime = null;
                                            Date endDateTime = null;
                                            Timestamp startTimestamp = document.getTimestamp("eventStartDateTime");
                                            Timestamp endTimestamp = document.getTimestamp("eventEndDateTime");

                                            if (startTimestamp != null) {
                                                startDateTime = startTimestamp.toDate();
                                            }
                                            if (endTimestamp != null) {
                                                endDateTime = endTimestamp.toDate();
                                            }

                                            Event event = new Event(
                                                    user,
                                                    document.getString("eventName"),
                                                    document.getString("description"),
                                                    document.getString("posterURI"),
                                                    maxAttendees,
                                                    document.getId(),
                                                    startDateTime,
                                                    endDateTime,
                                                    document.getString("address"),
                                                    document.getBoolean("active")
                                            );
                                            events.add(event);
                                        } else {
                                            Toast.makeText(requireContext(), "Organizer not found for event: " + document.getId(), Toast.LENGTH_SHORT).show();
                                        }
                                        if (i[0] == documents.size() - 1) {
                                            events.sort(new Comparator<Event>() {
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
                                        i[0]++;
                                    });
                                } else {
                                    if (i[0] == documents.size() - 1) {
                                        events.sort(new Comparator<Event>() {
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
                                    i[0]++;
                                }
                            }
                        } else {
                            eventListView.setVisibility(View.VISIBLE);
                            loadingProgressBar.setVisibility(View.GONE);
                            noEventsText.setVisibility(View.VISIBLE);
                            noEventsText.setText("There are no upcoming events");
                            eventAdapter.notifyDataSetChanged();
                        }
                    } else {
                        Toast.makeText(requireContext(), "Error loading events", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}

