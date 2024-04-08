package com.example.eventsnapqr;

import android.app.AlertDialog;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;

//import androidx.appcompat.app.AlertDialog;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

/**
 * fragment used to browse events as an administrator
 */
public class AdminBrowseEventsFragment extends Fragment {
    private ListView eventListView;
    private ImageView backButton;
    private ArrayAdapter<String> eventAdapter;
    private List<String> eventNames;
    private List<String> eventIds;
    private FirebaseFirestore db;
    ProgressBar loadingProgressBar;
    private List<HashMap<String, Object>> eventDataList;

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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_events, container, false);
        eventListView = view.findViewById(R.id.events);
        eventNames = new ArrayList<>();
        eventIds = new ArrayList<>();
        eventDataList = new ArrayList<>();
        backButton = view.findViewById(R.id.button_back_button);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

        db = FirebaseFirestore.getInstance();

        loadingProgressBar.setVisibility(View.VISIBLE);
        eventListView.setVisibility(View.INVISIBLE);

        FirebaseFirestore.getInstance().collection("events").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                eventIds.clear();
                eventNames.clear();
                eventDataList.clear();
                final int[] i = {0};
                Log.d("TAG", "snapshot");

                for (QueryDocumentSnapshot doc : value) {
                    HashMap<String, Object> data = new HashMap<>();
                    data.put("name", doc.get("eventName"));
                    data.put("id", doc.getId());
                    eventDataList.add(data);
                }
                eventDataList.sort(new Comparator<HashMap<String, Object>>() {
                    @Override
                    public int compare(HashMap<String, Object> o1, HashMap<String, Object> o2) {
                        String event1 = (String) o1.get("name");
                        event1 = event1.toLowerCase();
                        String event2 = (String) o2.get("name");
                        event2 = event2.toLowerCase();
                        return event1.compareTo(event2);
                    }
                });
                for (HashMap<String, Object> dataItem : eventDataList) {
                    eventIds.add((String) dataItem.get("id"));
                    eventNames.add((String) dataItem.get("name"));
                }
                eventAdapter.notifyDataSetChanged();
                loadingProgressBar.setVisibility(View.GONE);
                eventListView.setVisibility(View.VISIBLE);
            }
        });

        eventAdapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, eventNames);
        eventListView.setAdapter(eventAdapter);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        eventListView.setOnItemClickListener((parent, view1, position, id) -> {
            String eventId = eventIds.get(position);
            showEventDetailsDialog(eventId);
        });

        return view;
    }

    /**
     * alert dialog that gives the admin option to view an event details page or delete it
     * @param eventId the eventId
     */
    private void showEventDetailsDialog(String eventId) {
        FirebaseController firebaseController = FirebaseController.getInstance();
        firebaseController.getEvent(eventId, new FirebaseController.OnEventRetrievedListener() {
            @Override
            public void onEventRetrieved(Event event) {
                if (event != null) {
                    // Event retrieved successfully, show dialog with event details
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Event Details")
                            .setMessage("Event Name: " + event.getEventName() + "\n"
                                    + "Organizer Name: " + event.getOrganizer().getName() + "\n"
                                    + "Organizer ID: " + event.getOrganizer().getDeviceID() + "\n"
                                    + "Description: " + event.getDescription())
                            .setPositiveButton("View Event Page", (dialog, which) -> {
                                // Use the position parameter directly
                                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                                Bundle bundle = new Bundle();
                                bundle.putString("eventId", eventId);
                                bundle.putInt("position", -1); // denotes this page is coming from a different source (the admin page)
                                navController.navigate(R.id.action_adminBrowseEventsFragment_to_eventDetailFragment, bundle);

                            })
                            .setNegativeButton("Delete", (dialog, which) -> {
                                showDeleteConfirmationDialog(event);
                            })
                            .setNeutralButton("Cancel", null)
                            .create()
                            .show();
                }
            }
        });
    }

    /**
     * alert dialog used to confirm if the admin wants to delete the event
     * @param event the event object that may be deleted
     */
    private void showDeleteConfirmationDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete '" + event.getEventName() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> { // if yes
                    Runnable completionCallback = null;
                    try {
                        FirebaseController.getInstance().deleteEvent(event, (FirebaseController.FirestoreOperationCallback) completionCallback);
                        eventAdapter.notifyDataSetChanged();
                    }
                    catch (Exception e) {
                        Log.d("TAG", e.toString());
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }
}