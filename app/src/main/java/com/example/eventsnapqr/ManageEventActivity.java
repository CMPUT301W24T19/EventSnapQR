package com.example.eventsnapqr;

import static android.app.PendingIntent.getActivity;
import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.Toast;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ManageEventActivity extends AppCompatActivity {
    private FirebaseController firebaseController;
    private ListView attendeeListView, milestoneListView;
    private ArrayAdapter<String> eventAdapter, milestoneAdapter;
    private List<String> attendeeNames, milestoneList, attendeeIds, checkedInNames, checkedInIds;
    private List<Integer> attendeeCheckedIn;
    private FirebaseFirestore db;
    private String eventId;
    private View menuButton;
    private ImageView backButton;
    private Event currentEvent;
    private Switch filterSwitch;
    private Uri imageUri;
    private String uriString;
    private TextView totalAttendeesTextView;
    private TextView totalCheckedInTextView;
    private ActivityResultLauncher<PickVisualMediaRequest> choosePoster;
    private int checkedInCount;
    private int attendeeCount;
    private ProgressBar loadingProgressBar;
    private TextView eventNameTextView;
    private ExtendedFloatingActionButton fab;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_event);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        firebaseController = new FirebaseController();
        attendeeNames = new ArrayList<>();
        attendeeIds = new ArrayList<>();
        milestoneList = new ArrayList<>();
        eventAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, attendeeNames);
        milestoneAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, milestoneList);
        attendeeListView = findViewById(R.id.attendee_list);
        attendeeCheckedIn = new ArrayList<>();
        attendeeListView.setAdapter(eventAdapter);
        milestoneListView = findViewById(R.id.milestone_list);
        milestoneListView.setAdapter(milestoneAdapter);
        menuButton = findViewById(R.id.menu_button);
        backButton = findViewById(R.id.button_back_button);
        totalAttendeesTextView = findViewById(R.id.total_attendees_label);
        totalCheckedInTextView = findViewById(R.id.total_checked_in_label);
        filterSwitch = findViewById(R.id.filter_switch);
        loadingProgressBar = findViewById(R.id.loadingProgressBar);
        eventNameTextView = findViewById(R.id.page_name);
        fab = findViewById(R.id.make_announcements_fab);

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupMenu(v);
            }
        });

        if (getIntent() != null) {
            eventId = getIntent().getStringExtra("eventId");
        }

        loadingProgressBar.setVisibility(View.VISIBLE);
        attendeeListView.setVisibility(View.INVISIBLE);
        milestoneListView.setVisibility(View.INVISIBLE);
        menuButton.setVisibility(View.INVISIBLE);
        backButton.setVisibility(View.INVISIBLE);
        filterSwitch.setVisibility(View.INVISIBLE);
        totalAttendeesTextView.setVisibility(View.INVISIBLE);
        totalCheckedInTextView.setVisibility(View.INVISIBLE);
        eventNameTextView.setVisibility(View.INVISIBLE);
        fab.setVisibility(View.INVISIBLE);

        firebaseController.getEvent(eventId, new FirebaseController.OnEventRetrievedListener() {
            @Override
            public void onEventRetrieved(Event event) {
                if (event != null) {
                    currentEvent = event;
                    eventNameTextView.setText(currentEvent.getEventName());

                    fetchAttendeeData(true);
                } else {
                    Log.d("ManageEventActivity", "Failed to retrieve event");
                }
            }
        });

        db = FirebaseFirestore.getInstance();
        /*
        if (eventId != null) {
            DocumentReference eventDocRef = db.collection("events").document(eventId);
            eventDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String eventName = documentSnapshot.getString("eventName");
                    TextView eventNameTextView = findViewById(R.id.page_name);
                    eventNameTextView.setText(eventName);
                } else {
                    Log.d("ManageEventActivity", "No such document");
                }
            }).addOnFailureListener(e -> {
                Log.d("ManageEventActivity", "Error fetching event data: " + e.getMessage());
            });
        }*/

        // Set up the activity result launcher for choosing a poster
        choosePoster = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("TAG", "Selected URI: " + uri);
                imageUri = uri;
                uploadPoster(uri);
            } else {
                Log.d("TAG", "No media selected");
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showNotificationDialog();
            }
        });

        eventAdapter = new ArrayAdapter<String>(this, R.layout.list_attendees_layout, R.id.attendee_name, attendeeNames) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView attendeeNameTextView = view.findViewById(R.id.attendee_name);
                ImageView checkMarkImageView = view.findViewById(R.id.checkedIn_image);
                attendeeNameTextView.setText(attendeeNames.get(position));

                if (attendeeCheckedIn.get(position) >= 1) {
                    checkMarkImageView.setVisibility(View.VISIBLE);
                } else {
                    checkMarkImageView.setVisibility(View.GONE);
                }

                return view;
            }
        };

        filterSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (filterSwitch.isChecked()) {
                    fetchAttendeeData(false);
                } else {
                    fetchAttendeeData(true);
                }
            }
        });

        attendeeListView.setAdapter(eventAdapter);

        attendeeListView.setOnItemClickListener((parent, view1, position, id) -> attendeeDialog(position));
        updateTexts();
    }


    private void fetchMilestones() {
        firebaseController.getMilestones(eventId, new FirebaseController.MilestonesListener() {
            @Override
            public void onMilestonesLoaded(List<String> milestones) {
                processMilestones(milestones);
                loadingProgressBar.setVisibility(View.INVISIBLE);
                attendeeListView.setVisibility(View.VISIBLE);
                milestoneListView.setVisibility(View.VISIBLE);
                menuButton.setVisibility(View.VISIBLE);
                backButton.setVisibility(View.VISIBLE);
                filterSwitch.setVisibility(View.VISIBLE);
                totalAttendeesTextView.setVisibility(View.VISIBLE);
                totalCheckedInTextView.setVisibility(View.VISIBLE);
                eventNameTextView.setVisibility(View.VISIBLE);
                fab.setVisibility(View.VISIBLE);
            }
        });
    }

    private void processMilestones(List<String> milestones) {
        milestoneList.clear();
        milestoneList.addAll(milestones);
        milestoneAdapter.notifyDataSetChanged();
    }

    /**
     * a function to populate the attendees listView
     */
    private void fetchAttendeeData(Boolean checkedIn) {
        db = FirebaseFirestore.getInstance();
        CollectionReference attendeesRef = db.collection("events").document(eventId).collection("attendees");
        attendeeNames.clear();
        attendeeIds.clear();
        attendeeCheckedIn.clear();

        checkedInCount = 0;
        attendeeCount = 0;
        AtomicInteger retrievalCounter = new AtomicInteger(0);

        attendeesRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
            int totalAttendees = queryDocumentSnapshots.size();

            for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                String attendeeId = documentSnapshot.getId();
                attendeeCount++;

                Long longValue = documentSnapshot.getLong("checkedIn");
                Integer numCheckIns = longValue != null ? longValue.intValue() : 0;

                if (checkedIn || numCheckIns > 0) { // Check if checkedIn is true or user is already checked in
                    DocumentReference checkedInRef = attendeesRef.document(attendeeId).collection("checkedIn").document("check");
                    checkedInRef.get().addOnSuccessListener(checkedInDocumentSnapshot -> {
                        if (numCheckIns > 0) {
                            checkedInCount++;
                        }

                        firebaseController.getUser(attendeeId, user -> {
                            if (user != null) {
                                attendeeNames.add(user.getName());
                                attendeeIds.add(user.getDeviceID());
                                attendeeCheckedIn.add(numCheckIns); // Add checked in count
                                eventAdapter.notifyDataSetChanged();

                                if (retrievalCounter.incrementAndGet() == totalAttendees) {
                                    updateTexts();
                                    fetchMilestones();
                                }
                            }
                        });
                    }).addOnFailureListener(e -> {
                        retrievalCounter.incrementAndGet(); // Increment even on failure to keep track
                        if (retrievalCounter.get() == totalAttendees) {
                            updateTexts();
                        }
                    });
                } else {
                    retrievalCounter.incrementAndGet(); // Increment for non-checked-in users
                    if (retrievalCounter.get() == totalAttendees) {
                        updateTexts();
                    }
                }
            }
        }).addOnFailureListener(e -> {
            Log.d("FetchAttendeeData", "Error fetching attendee data: " + e.getMessage());
        });
    }

    private void updateTexts() {
        totalAttendeesTextView.setText("Total Attendees: " + attendeeCount);
        totalCheckedInTextView.setText("Total Checked-In: " + checkedInCount);
    }

    /**
     * alert dialog to present the number of times a user has checked into the event
     * @param position
     */
    private void attendeeDialog(Integer position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        String attendeeName = attendeeNames.get(position);
        Integer timesCheckedIn = attendeeCheckedIn.get(position);
        String timesString = timesCheckedIn == 1 ? "time" : "times";

        builder.setMessage(attendeeName + " has checked-in " + timesCheckedIn + " " + timesString + ".")
                .setPositiveButton("View Profile", (dialog, id) -> {
                    // lead to fragment_view_user_profile
                })
                .setNegativeButton("Remove Attendee", (dialog, id) -> {
                    showDeleteConfirmationDialog(attendeeIds.get(position));
                });
        builder.create().show();
    }

    /**
     * alert dialog used to confirm if the admin wants to delete the event
     * @param attendeeId the user object that may be removed from the event
     */
    private void showDeleteConfirmationDialog(String attendeeId) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(ManageEventActivity.this);
        builder.setTitle("Confirm Removal")
                .setMessage("Are you sure you want to remove '" + attendeeId + "' from your event?")
                .setPositiveButton("Yes", (dialog, which) -> { // if yes
                    Runnable completionCallback = null;
                    try {
                        // delete the attendee from the event
                    }
                    catch (Exception e) {
                        Log.d("TAG", e.toString());
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }


    /**
     * displays dialog for an organizer to make an announcement
     */
    private void showNotificationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_notification, null);
        builder.setView(dialogView);

        EditText editTextAnnouncement = dialogView.findViewById(R.id.editTextAnnouncement);
        Switch switchEnableNotifications = dialogView.findViewById(R.id.switchEnableNotifications);

        builder.setTitle("Make Announcement");
        builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String announcement = editTextAnnouncement.getText().toString();

                CollectionReference announcementsRef = db.collection("events").document(currentEvent.getEventID()).collection("announcements");
                Map<String, Object> announcementData = new HashMap<>();
                announcementData.put("message", announcement);
                announcementData.put("timestamp", new Date());

                announcementsRef.add(announcementData)
                        .addOnSuccessListener(documentReference -> Toast.makeText(getApplicationContext(), "Announcement sent successfully", Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e -> Toast.makeText(getApplicationContext(), "Failed to send announcement", Toast.LENGTH_SHORT).show());
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


    /**
     * uploads selected image to the database
     * @param uri uri of the selected image
     */
    private void uploadPoster(Uri uri) {
        Log.d("ManageEventFragment", "Current Poster URI before update: " + currentEvent.getPosterURI());
        if (uri != null) {
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("posters/" + currentEvent.getEventID() + ".jpg");
            storageRef.putFile(uri)
                    .addOnSuccessListener(taskSnapshot -> {
                        storageRef.getDownloadUrl().addOnSuccessListener(downloadUri -> {
                            uriString = downloadUri.toString();
                            DocumentReference eventRef = db.collection("events").document(currentEvent.getEventID());
                            eventRef.update("posterURI", uriString)
                                    .addOnSuccessListener(aVoid -> {
                                        currentEvent.setPosterURI(uriString);
                                        Toast.makeText(ManageEventActivity.this, "Poster Updated", Toast.LENGTH_SHORT).show();
                                        Log.d("ManageEventFragment", "Current Poster URI after update: " + currentEvent.getPosterURI());
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.d("ManageEventFragment", "Error updating posterURI: " + e.getMessage());
                                    });
                        }).addOnFailureListener(e -> {
                            Log.d("ManageEventFragment", "Error getting download URL: " + e.getMessage());
                        });
                    })
                    .addOnFailureListener(e -> {
                        Log.d("ManageEventFragment", "Error uploading image: " + e.getMessage());
                    });
        } else {
            Toast.makeText(ManageEventActivity.this, "No image was selected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Displays and handles the options when clicking the vertical 3 dots.
     */
    public void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(ManageEventActivity.this, view); // Use the provided view as the anchor
        popupMenu.getMenuInflater().inflate(R.menu.menu_manage_event, popupMenu.getMenu());
        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.view_qr) { // view QR code again
                Bundle bundle = new Bundle();
                bundle.putString("eventId", eventId);
                bundle.putString("destination", "manage");
                QRDialogFragment qrDialogFragment = new QRDialogFragment();
                qrDialogFragment.setArguments(bundle);
                qrDialogFragment.show(getSupportFragmentManager(), "qr_dialog_fragment");
                return true;
            } else if (itemId == R.id.upload_poster) { // modify the associated poster
                choosePoster.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
                return true;
            } else if (itemId == R.id.remove_poster) { // remove the associated poster
                if (currentEvent.getPosterURI() == null) {
                    Toast.makeText(ManageEventActivity.this, "No Poster to Remove", Toast.LENGTH_SHORT).show();
                } else {
                    FirebaseController.getInstance().deleteImage(currentEvent.getPosterURI(), currentEvent, ManageEventActivity.this);
                    Toast.makeText(ManageEventActivity.this, "Poster Updated", Toast.LENGTH_SHORT).show();
                }

                return true;
            } else if (itemId == R.id.view_map) { // view map
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.your_event_constrained_layout, new MapFragment());
                fragmentTransaction.addToBackStack("manage_activity");
                fragmentTransaction.commit();

                return true;
            } else {
                return false;
            }
        });
        popupMenu.show();
    }
}