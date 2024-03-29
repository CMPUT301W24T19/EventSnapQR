package com.example.eventsnapqr;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * fragment to show the details of an event, including the name of the organizer,
 * the description, announcements and max attendees. gives the option for the user to
 * sign up for the event.
 *
 * currently a user can sign up for an event infinite times,
 * however, the data is only uploaded to the database once
 */
public class EventDetailFragment extends Fragment {
    private String eventId;
    private String androidId;
    private TextView eventName;
    private TextView eventDescription;
    private ImageView eventPosterImage;
    private TextView eventOrganizer;
    private TextView eventMaxAttendees;
    private TextView eventAnnouncement;
    private Integer position;

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
            position = getArguments().getInt("position");
            loadEventDetails(eventId);

        }
        Log.d("position in detail", "position: " + position);
        androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

    }

    /**
     * Setup actions to be taken upon view creation and when the views are interacted with.
     * Handles and validates the sign up button press where the user is added to the events
     * attendee list in the firebase
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);
        getActivity().getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        );

        FirebaseController.getInstance().isUserSignedUp(androidId, eventId, new FirebaseController.OnSignUpCheckListener() {
            @Override
            public void onSignUpCheck(boolean isSignedUp) {
                if (isSignedUp) {
                    view.findViewById(R.id.sign_up_button).setVisibility(View.GONE);
                    TextView signUpMessage = view.findViewById(R.id.sign_up_message);
                    signUpMessage.setVisibility(View.VISIBLE);
                }
            }
        });

        view.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (position == -1) {
                    requireActivity().onBackPressed();
                } else {
                    Intent intent = requireActivity().getIntent();
                    intent.putExtra("position", position); // ensure the right tab is opened upon returning
                    requireActivity().finish();
                    startActivity(intent);
                }
            }
        });

        view.findViewById(R.id.sign_up_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseController.getInstance().getUser(androidId, new FirebaseController.OnUserRetrievedListener() {
                    @Override
                    public void onUserRetrieved(User user) {
                        if (user != null) {
                            FirebaseController.getInstance().getEvent(eventId, new FirebaseController.OnEventRetrievedListener() {
                                @Override
                                public void onEventRetrieved(Event event) {
                                    if (event != null) {
                                        String eventId = event.getEventID();

                                        CollectionReference attendeesCollectionRef = FirebaseFirestore.getInstance()
                                                .collection("events")
                                                .document(eventId)
                                                .collection("attendees");

                                        attendeesCollectionRef.get()
                                                .addOnSuccessListener(queryDocumentSnapshots -> {
                                                    int numberOfAttendees = queryDocumentSnapshots.size();
                                                    Log.d("Attendees", "Number of attendees: " + numberOfAttendees);

                                                    Log.d("ATT LIST", "onEventRetrieved: " + numberOfAttendees);
                                                    if (event.getMaxAttendees() == null || numberOfAttendees < event.getMaxAttendees()) {
                                                        FirebaseController.getInstance().addAttendeeToEvent(event, user);
                                                        FirebaseController.getInstance().addPromiseToGo(user, event);
                                                        CreateDialog(event.getEventName());
                                                    } else {
                                                        Toast.makeText(requireContext(), "Event is full", Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(e -> {
                                                    Log.e("Error", "Error getting attendee count", e);
                                                });
                                    } else {
                                        Toast.makeText(requireContext(), "Failed to retrieve event details", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            Toast.makeText(requireContext(), "Failed to retrieve user details", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });

        return view;
    }


    /**
     * after the sign up button is pressed this dialog notifies the user
     * that they have signed up for the given event
     * @param eventName name of the event
     */
    public void CreateDialog(String eventName) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle ("You have successfully signed up for " + eventName)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Back to main page
                        requireActivity().finish();
                    }
                });

        builder.create().show();
    }

    /**
     * interacts with the firebase controller to retrieve the specified event
     * @param eventIdentifier ID of the event
     */
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

    /**
     * sets the views with the necessary data
     * @param event the event object to be displayed
     */
    private void displayEventDetails(Event event) {
        eventPosterImage = getView().findViewById(R.id.event_poster);
        Glide.with(requireContext())
                .load(event.getPosterURI())
                .placeholder(R.drawable.place_holder_img)
                .dontAnimate()
                .into(eventPosterImage);

        eventDescription = getView().findViewById(R.id.description_content);
        eventDescription.setText(event.getDescription());

        eventName = getView().findViewById(R.id.page_name);
        eventName.setText(event.getEventName());

        eventOrganizer = getView().findViewById(R.id.organizer_content);
        eventOrganizer.setText(event.getOrganizer().getName());

        eventMaxAttendees = getView().findViewById(R.id.max_attendees_content);
        eventMaxAttendees.setText(event.getMaxAttendees() != null ? event.getMaxAttendees().toString() : "N/A");

        eventAnnouncement = getView().findViewById(R.id.announce_content);
        StringBuilder announcementText = new StringBuilder();
        List<String> announcements = event.getAnnouncements();
        if (announcements != null && !announcements.isEmpty()) {
            for (String announcement : announcements) {
                announcementText.append("\u2022 ").append(announcement).append("\n"); // Prefix each announcement with a bullet point
            }
        } else {
            announcementText.append("No announcements available");
        }
        eventAnnouncement.setText(announcementText.toString());
    }

}
