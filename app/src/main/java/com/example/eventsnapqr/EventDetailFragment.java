package com.example.eventsnapqr;

import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.Firebase;

public class EventDetailFragment extends Fragment {
    public EventDetailFragment() {
        // Required empty public constructor
    }

    private String eventId;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
            loadEventDetails(eventId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);
        view.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        view.findViewById(R.id.sign_up_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CreateDialog(eventId);
            }
        });

        return view;
    }

    public void CreateDialog(String eventName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("You have successfully signed up for " + eventName)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Back to main page
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.mainPageFragment);
                    }
                });

        builder.create().show();
    }

    private void loadEventDetails(String eventIdentifier) {
        FirebaseController.getInstance().getEvent(eventIdentifier, new FirebaseController.OnEventRetrievedListener() {
            @Override
            public void onEventRetrieved(Event event) {
                if (event != null) {
                    // Event details retrieved successfully, update UI with event details
                    displayEventDetails(event);
                } else {
                    Toast.makeText(requireContext(), "Failed to retrieve event details", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void displayEventDetails(Event event) {
        // Update UI elements with event details
        TextView eventDescription = getView().findViewById(R.id.description_content);
        eventDescription.setText(event.getDescription());

        TextView eventName = getView().findViewById(R.id.page_name);
        eventName.setText(event.getEventName());

        TextView eventOrganizer = getView().findViewById(R.id.organizer_content);
        eventOrganizer.setText(event.getOrganizer().getName());

        TextView eventMaxAttendees = getView().findViewById(R.id.max_attendees_content);
        eventMaxAttendees.setText(event.getMaxAttendees() != null ? event.getMaxAttendees().toString() : "No Max Attendees");
    }
}
