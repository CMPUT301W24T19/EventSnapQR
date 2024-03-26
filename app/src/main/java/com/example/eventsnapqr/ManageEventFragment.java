package com.example.eventsnapqr;

import android.content.DialogInterface;
import android.content.Intent;
import android.widget.Switch;
import android.widget.Toast;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * fragment that shows an organizer all the options to do with managing an event. This includes
 * viewing their attendees and the number of times they've checked in, the events unique QR code,
 * a map of their checked-in attendees (not yet implemented), and the ability to send notifications
 * to all their attendees (not yet implemented)
 */
public class ManageEventFragment extends Fragment {
    private FirebaseController firebaseController;
    private ListView attendeeListView, milestoneListView;
    private ArrayAdapter<String> eventAdapter, milestoneAdapter;
    private List<String> attendeeNames, milestoneList;
    private List<Integer> attendeeCheckedIn;
    private FirebaseFirestore db;
    private String eventId;
    private View menuButton;
    private Event currentEvent;
    private Uri imageUri;
    private String uriString;
    private ActivityResultLauncher<PickVisualMediaRequest> choosePoster;


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
        milestoneList = new ArrayList<>();
        eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, attendeeNames);
        milestoneAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, milestoneList);

        // Call getEvent method and assign the result to currentEvent
        firebaseController.getEvent(eventId, new FirebaseController.OnEventRetrievedListener() {
            @Override
            public void onEventRetrieved(Event event) {
                if (event != null) {
                    currentEvent = event;
                } else {
                    Log.d("ManageEventFragment", "Failed to retrieve event");
                }
            }
        });
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
        milestoneListView = view.findViewById(R.id.milestone_list);
        milestoneAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, milestoneList);
        milestoneListView.setAdapter(milestoneAdapter);
        menuButton = view.findViewById(R.id.menu_button);
        fetchAttendeeData();
        fetchMilestones();

         choosePoster = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("TAG", "Selected URI: " + uri);
                        imageUri = uri;
                    } else {
                        Log.d("TAG", "No media selected");
                    }
                });

        view.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = requireActivity().getIntent();
                intent.putExtra("position", 2); // ensure the right tab is opened upon returning
                requireActivity().finish();
                startActivity(intent);
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        attendeeListView.setOnItemClickListener((parent, view1, position, id) -> attendeeDialog(position));

        return view;
    }
    private void fetchMilestones(){
        firebaseController.getMilestones(eventId, new FirebaseController.MilestonesListener() {
            @Override
            public void onMilestonesLoaded(List<String> milestones) {
                processMilestones(milestones);
            }
        });
    }
    private void processMilestones(List<String> milestones){
        milestoneList.clear();
        milestoneList.addAll(milestones);
        milestoneAdapter.notifyDataSetChanged();
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
    private void attendeeDialog(Integer position) {
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

    private void showNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_notification, null);
        builder.setView(dialogView);

        EditText editTextAnnouncement = dialogView.findViewById(R.id.editTextAnnouncement);
        Switch switchEnableNotifications = dialogView.findViewById(R.id.switchEnableNotifications);

        builder.setTitle("Make Announcement");
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String announcement = editTextAnnouncement.getText().toString();
                boolean enableNotifications = switchEnableNotifications.isChecked();

                // Get reference to the 'announcements' subcollection under the event document
                CollectionReference announcementsRef = db.collection("events").document(currentEvent.getEventID()).collection("announcements");

                // Add the announcement as a document with its own ID
                announcementsRef.document(announcement)
                        .set(new HashMap<>())
                        .addOnSuccessListener(documentReference -> {
                            // Successfully added announcement to subcollection
                            Toast.makeText(requireContext(), "Announcement sent successfully", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            // Handle failure to add announcement to subcollection
                            Log.d("ManageEventFragment", "Error adding announcement to subcollection: " + e.getMessage());
                            Toast.makeText(requireContext(), "Failed to send announcement", Toast.LENGTH_SHORT).show();
                        });
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.show();
    }

    private void uploadPoster() {
        choosePoster.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
        if (imageUri != null) {
            uriString = imageUri.toString();
        } else {
            Toast.makeText(requireContext(), "No image was selected", Toast.LENGTH_SHORT).show();
        }
        DocumentReference eventRef = db.collection("events").document(currentEvent.getEventID());
        eventRef.update("posterURI", uriString)
                .addOnSuccessListener(aVoid -> {
                    currentEvent.setPosterURI(uriString); // Update locally
                })
                .addOnFailureListener(e -> {
                    Log.d("ManageEventFragment", "Error updating posterURI: " + e.getMessage());
                });
    }

    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_manage_event, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId(); // Get the selected item ID

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);

            if (itemId == R.id.view_qr) { // view qr code again
                Bundle qrBundle = new Bundle();
                qrBundle.putString("eventId", eventId);
                qrBundle.putString("destination", "manage");
                navController.navigate(R.id.action_ManageEventFragment_to_qRDialogFragment, qrBundle);
                return true;
            } else if (itemId == R.id.upload_poster) { // modify the associated poster
                uploadPoster();
                return true;
            } else if (itemId == R.id.remove_poster) { // remove the associated poste
                FirebaseController.getInstance().deleteImage(currentEvent.getPosterURI(), currentEvent);
                return true;
            } else if (itemId == R.id.make_announcement) {
                showNotificationDialog();
                return true;
            } else if (itemId == R.id.view_map) {
                navController.navigate(R.id.action_ManageEventFragment_to_MapFragment);
                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }
}
