package com.example.eventsnapqr;

import static androidx.core.content.ContentProviderCompat.requireContext;
import static androidx.core.content.ContextCompat.startActivity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import static java.security.AccessController.getContext;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

//import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AdminBrowseEventsFragment extends Fragment {
    private ListView eventListView;
    private ArrayAdapter<String> eventAdapter;
    private List<String> eventNames;
    private List<String> eventIds;
    private FirebaseFirestore db;
    private Button searchButton;
    private EditText searchBar;
    private ArrayList<String> viewList = new ArrayList<>();
    //edit this one
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_events, container, false);
        eventListView = view.findViewById(R.id.events);
        eventNames = new ArrayList<>();
        searchBar = view.findViewById(R.id.search_bar);
        eventIds = new ArrayList<>();
        searchButton = view.findViewById(R.id.button_search);
        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewList.clear();

                for(String eventName: eventNames){
                    if(eventName.contains(searchBar.getText())){
                        viewList.add(eventName);
                    }
                }
                if(!viewList.isEmpty()){
                    eventAdapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1,viewList );
                    eventListView.setAdapter(eventAdapter);

                }
            }
        });
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

        eventListView.setOnItemClickListener((parent, view1, position, id) -> {
            String eventId = eventIds.get(position);
            showEventDetailsDialog(eventId, position);
        });

        return view;
    }

    public void gotoMyEventActivity() {
        Intent intent = new Intent(getContext(), MyEventActivity.class);
        startActivity(intent);
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
                    } // error handling?
                });
    }

    private void showDeleteConfirmationDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete '" + event.getEventName() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> { // if yes
                    FirebaseController.getInstance().deleteEvent(event);
                    eventAdapter.notifyDataSetChanged();
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }
}
