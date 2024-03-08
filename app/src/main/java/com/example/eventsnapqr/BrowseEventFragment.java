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
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.List;

/**
 * fragment for attendees to browse any events that are are currently posted. comes from the main
 * page fragment and can lead to either an events detail page or a users current events page.
 */
public class BrowseEventFragment extends Fragment {
    private ListView eventListView; // list of events
    private ArrayAdapter<String> eventAdapter;
    private List<String> eventNames; // list of event names
    private List<String> eventIds; // list of event ids
    private FirebaseFirestore db; // database instance

    /**
     * retrieve any events that are currently in the database.
     */
    private void loadEvents() {
        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventNames.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            eventIds.add(document.getId());
                            eventNames.add(document.getString("eventName"));
                        }
                        eventAdapter.notifyDataSetChanged();
                    }
                    else {
                        Toast.makeText(requireContext(), "Error loading events", Toast.LENGTH_SHORT).show();
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
        eventNames = new ArrayList<>();
        eventIds = new ArrayList<>();
        eventAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, eventNames);
        eventListView.setAdapter(eventAdapter);
        db = FirebaseFirestore.getInstance();
        loadEvents();

        view.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.view_on_events_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_browseEventFragment_to_myEventFragment);
            }
        });

        eventListView.setOnItemClickListener((parent, view1, position, id) -> {
            String eventId = eventIds.get(position);
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventId);
            navController.navigate(R.id.action_browseEventFragment_to_eventDetailFragment, bundle);
        });

        return view;
    }
}
