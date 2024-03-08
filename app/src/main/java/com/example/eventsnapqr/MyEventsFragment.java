package com.example.eventsnapqr;

import static androidx.fragment.app.FragmentManager.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

/**
 * fragment for a user to browse all the events they are currently signed up to attend, and
 * the events they have organized. if an attending event is pressed, you are brought to the
 * event details page. if an organized event is pressed you are brought to "your event fragment"
 */
public class MyEventsFragment extends Fragment {
    private ListView attend_eventListView, organize_eventListView;
    private ArrayAdapter<String> attend_eventAdapter, organize_eventAdapter;
    private String androidId;
    private List<String> attend_eventNames, organize_eventNames;
    private FirebaseFirestore db;

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
     * @return resulting view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_events, container, false);
        attend_eventListView = v.findViewById(R.id.attending_events_list);
        organize_eventListView = v.findViewById(R.id.organized_events_list);
        androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        attend_eventNames = new ArrayList<>();
        organize_eventNames = new ArrayList<>();
        attend_eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, attend_eventNames);
        organize_eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, organize_eventNames);
        attend_eventListView.setAdapter(attend_eventAdapter);
        organize_eventListView.setAdapter(organize_eventAdapter);

        db = FirebaseFirestore.getInstance();

        v.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
            }
        });

        fetchEvents("organizedEvents", organize_eventNames, organize_eventAdapter);
        fetchEvents("promisedEvents", attend_eventNames, attend_eventAdapter);

        return v;
    }

    /**
     * fetch the events that the user is attending and organizing by interacting with the database
     * @param subcollection name of the subcollection to fetch
     * @param eventNames list of event names
     * @param eventAdapter adapter for event strings
     */
    private void fetchEvents(String subcollection, List<String> eventNames, ArrayAdapter<String> eventAdapter) {
        db.collection("users")
                .document(androidId)
                .collection(subcollection)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventNames.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId();
                            db.collection("events")
                                    .document(eventId)
                                    .get()
                                    .addOnSuccessListener(eventDocument -> {
                                        if (eventDocument != null && eventDocument.exists()) {
                                            String eventName = eventDocument.getString("eventName");
                                            eventNames.add(eventName);
                                            eventAdapter.notifyDataSetChanged();
                                        } else {
                                            Log.d(TAG, "fetchEvents: Event document not found for eventId: " + eventId);
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e("FETCH ERROR", "Error fetching event document", e);
                                    });
                        }
                    } else {
                        Log.e("FETCH ERROR", "Error fetching subcollection documents", task.getException());
                    }
                });
    }
}