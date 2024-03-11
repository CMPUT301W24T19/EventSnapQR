package com.example.eventsnapqr;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.util.ArrayList;
import java.util.List;

/**
 * fragment that shows an organizer all the options to do with managing an event. This includes
 * viewing their attendees and the number of times they've checked in, the events unique QR code,
 * a map of their checked-in attendees (not yet implemented), and the ability to send notifications
 * to all their attendees (not yet implemented)
 */
public class ManageEventFragment extends Fragment {
    private FirebaseController firebaseController;
    private ListView attendeeListView;
    private ArrayAdapter<String> eventAdapter;
    private List<String> attendeeNames;
    private List<Integer> attendeeCheckedIn;
    private FirebaseFirestore db;
    private String eventId;

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventId = getArguments().getString("eventId");
        }
        firebaseController = new FirebaseController();
        attendeeNames = new ArrayList<>();
        eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, attendeeNames);
    }

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
        View view = inflater.inflate(R.layout.fragment_manage_event, container, false);

        TextView eventNameTextView = view.findViewById(R.id.page_name);
        eventNameTextView.setText("Loading...");

        db = FirebaseFirestore.getInstance();
        DocumentReference eventDocRef = db.collection("events").document(eventId);
        eventDocRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String eventName = documentSnapshot.getString("eventName");
                eventNameTextView.setText(eventName);
            } else {
                Log.d("ManageEventFragment", "No such document");
            }
        }).addOnFailureListener(e -> {
            Log.d("ManageEventFragment", "Error fetching event data: " + e.getMessage());
        });

        attendeeListView = view.findViewById(R.id.attendee_list);
        attendeeNames = new ArrayList<>();
        attendeeCheckedIn = new ArrayList<>();
        eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, attendeeNames);
        attendeeListView.setAdapter(eventAdapter);

        fetchAttendeeData();

        view.findViewById(R.id.button_back_button).setOnClickListener(v -> requireActivity().onBackPressed());

        view.findViewById(R.id.real_time_attendance_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_YourEventFragment_to_RealTimeAttendanceFragment);
        });

        view.findViewById(R.id.attendee_map_button).setOnClickListener(v -> {
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_YourEventFragment_to_MapFragment);
        });

        view.findViewById(R.id.qr_code_button).setOnClickListener(v -> {
            String eventID = FirebaseController.getInstance().getUniqueEventID();
            Bundle bundle = new Bundle();
            bundle.putString("eventId", eventID);
            // navigate to QR dialog fragment here
        });

        view.findViewById(R.id.notify_attendee_button).setOnClickListener(v -> showNotificationDialog());

        attendeeListView.setOnItemClickListener((parent, view1, position, id) -> CreateDialog(position));

        return view;
    }

    /**
     * a function to populate the attendees listView
     */
    private void fetchAttendeeData() {
        db = FirebaseFirestore.getInstance();
        CollectionReference attendeesRef = db.collection("events").document(eventId).collection("attendees");

        attendeesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String attendeeId = documentSnapshot.getId();
                Long longValue = documentSnapshot.getLong("checkedIn");
                Integer numCheckIns = longValue != null ? longValue.intValue() : null;

                // Assuming you have a method to retrieve user details by ID
                firebaseController.getUser(attendeeId, user -> {
                    if (user != null) {
                        // Add user name to the attendeeNames list
                        attendeeNames.add(user.getName());
                        attendeeCheckedIn.add(numCheckIns);
                        eventAdapter.notifyDataSetChanged();
                    }
                });
            }
        }).addOnFailureListener(e -> {
            // Handle failure
            Log.d("FetchAttendeeData", "Error fetching attendee data: " + e.getMessage());
        });
    }

    /**
     * alert dialog to present the number of times a user has checked into the event
     * @param position
     */
    private void CreateDialog(Integer position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        String attendeeName = attendeeNames.get(position);
        Integer timesCheckedIn = attendeeCheckedIn.get(position);
        builder.setMessage(attendeeName + " has checked in your event " + timesCheckedIn + " times.")
                .setPositiveButton("OK", (dialog, id) -> {

                })
                .setNegativeButton("View on Map", (dialog, id) -> {

                });

        builder.create().show();
    }

    /**
     * alert dialog to present the organizer with the option to send a notification
     */
    private void showNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        final EditText input = new EditText(getContext());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(input);
        builder.setPositiveButton("Send", (dialog, which) -> {
            String notificationText = input.getText().toString();
            // Handle "Send" button click
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.setTitle("Notification");
        builder.setMessage("Enter notification message:");
        builder.show();
    }
}
