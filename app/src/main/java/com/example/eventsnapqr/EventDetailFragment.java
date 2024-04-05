package com.example.eventsnapqr;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Html;
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
import com.google.android.material.textfield.TextInputEditText;
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
    private ImageView eventPosterImage;
    private TextView eventName;
    private TextInputEditText eventOrganizer;
    private TextInputEditText eventDescription;
    private TextInputEditText eventLocation;
    private TextInputEditText eventMaxAttendees;
    private TextInputEditText eventAnnouncements;
    private TextInputEditText eventStartDateTime;
    private TextInputEditText eventEndDateTime;
    private Integer position;
    private Boolean toMain;

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
            toMain = getArguments().getBoolean("toMain");
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

        eventPosterImage = view.findViewById(R.id.eventPosterImageView);
        eventName = view.findViewById(R.id.eventNameTextView);
        eventOrganizer = view.findViewById(R.id.editTextOrganizerName);
        eventDescription = view.findViewById(R.id.editTextDescription);
        eventLocation = view.findViewById(R.id.editTextLocation);
        eventMaxAttendees = view.findViewById(R.id.editTextMaxAttendees);
        eventAnnouncements = view.findViewById(R.id.editTextAnnouncements);
        eventStartDateTime = view.findViewById(R.id.editTextStartDateTime);
        eventEndDateTime = view.findViewById(R.id.editTextEndDateTime);

        FirebaseController.getInstance().isUserSignedUp(androidId, eventId, new FirebaseController.OnSignUpCheckListener() {
            @Override
            public void onSignUpCheck(boolean isSignedUp) {
                if (isSignedUp) {
                    view.findViewById(R.id.sign_up_button).setVisibility(View.INVISIBLE);
                    TextView signUpMessage = view.findViewById(R.id.sign_up_message);
                    signUpMessage.setVisibility(View.VISIBLE);
                }
            }
        });

        view.findViewById(R.id.back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toMain){
                    Intent intent = new Intent(getActivity(), MainActivity.class);
                    startActivity(intent);
                }
                else if (position == -1) {
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
        eventPosterImage = getView().findViewById(R.id.eventPosterImageView);
        Glide.with(requireContext())
                .load(event.getPosterURI())
                .placeholder(R.drawable.place_holder_img)
                .into(eventPosterImage);

        eventName.setText(event.getEventName() != null ? event.getEventName() : "N/A");
        eventOrganizer.setText(event.getOrganizer() != null && event.getOrganizer().getName() != null ? event.getOrganizer().getName() : "N/A");
        eventDescription.setText(event.getDescription() != null ? event.getDescription() : "N/A");

        if (event.getEventStartDateTime() != null) {
            eventStartDateTime.setText(event.getEventStartDateTime().toString());
        } else {
            eventStartDateTime.setText("N/A");
        }

        if (event.getEventEndDateTime() != null) {
            eventEndDateTime.setText(event.getEventEndDateTime().toString());
        } else {
            eventEndDateTime.setText("N/A");
        }

        eventLocation.setText("N/A");

        List<String> announcements = event.getAnnouncements();
        if (announcements != null && !announcements.isEmpty()) {
            StringBuilder announcementsText = new StringBuilder();
            for (String announcement : announcements) {
                announcementsText.append("• ").append(announcement).append("<br>");
            }
            eventAnnouncements.setText(Html.fromHtml(announcementsText.toString(), Html.FROM_HTML_MODE_COMPACT));
        } else {
            eventAnnouncements.setText("No Announcements");
        }
        eventAnnouncements.setHint(null);

        eventMaxAttendees.setText(event.getMaxAttendees() != null ? String.valueOf(event.getMaxAttendees()) : "N/A");
    }


}
