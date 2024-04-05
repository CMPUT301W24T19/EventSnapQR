package com.example.eventsnapqr;

import android.app.AlertDialog;
import android.os.Bundle;

import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

//import androidx.appcompat.app.AlertDialog;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
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
    private ArrayList<String> viewList = new ArrayList<>();

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
        backButton = view.findViewById(R.id.button_back_button);
        loadingProgressBar = view.findViewById(R.id.loadingProgressBar);

        db = FirebaseFirestore.getInstance();

        loadingProgressBar.setVisibility(View.VISIBLE);
        backButton.setVisibility(View.INVISIBLE);
        eventListView.setVisibility(View.INVISIBLE);

        FirebaseFirestore.getInstance().collection("events").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                eventIds.clear();
                eventNames.clear();
                for (QueryDocumentSnapshot doc: value) {
                    FirebaseController.getInstance().getEvent(doc.getId(), new FirebaseController.OnEventRetrievedListener() {
                        @Override
                        public void onEventRetrieved(Event event) {
                            if (event.isActive()) {
                                eventIds.add(event.getEventID());
                                eventNames.add(event.getEventName());
                                eventAdapter.notifyDataSetChanged();
                            }
                        }
                    });
                }
            }
        });

        loadingProgressBar.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.VISIBLE);
        eventListView.setVisibility(View.VISIBLE);

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
            showEventDetailsDialog(eventId, position);
        });

        return view;
    }

    private void showEventDetailsDialog(String eventId, int position) {
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
                            .setPositiveButton("View Page", (dialog, which) -> {
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