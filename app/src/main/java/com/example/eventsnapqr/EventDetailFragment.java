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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

/**
 * fragment to show the details of an event, including the name of the organizer,
 * the description, announcements and max attendees. gives the option for the user to
 * sign up for the event.
 */
public class EventDetailFragment extends Fragment {
    private String eventId, androidId, organizerId;
    private ImageView eventPosterImage, backButton, checkMarkImageView;
    private TextView signUpMessage;
    private TextInputEditText eventName, eventOrganizer, eventDescription, eventMaxAttendees,
            eventAnnouncements, eventStartDateTime, eventEndDateTime, eventAddress;
    private TextInputLayout nameTextInputLayout, organizerTextInputLayout, descTextInputLayout,
            maxTextInputLayout, announceTextInputLayout, startTextInputLayout,
            endTextInputLayout, addressTextInputLayout;
    private ExtendedFloatingActionButton signUpButton;
    private Integer position;
    private Boolean toMain;
    private ProgressBar progressBar;
    private boolean checkedIn, signedUp;

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
        eventName = view.findViewById(R.id.editTextEventName);
        eventOrganizer = view.findViewById(R.id.editTextUserOrganizer);
        eventDescription = view.findViewById(R.id.editTextDescription);
        eventMaxAttendees = view.findViewById(R.id.editTextMaxAttendees);
        eventAnnouncements = view.findViewById(R.id.editTextAnnouncements);
        eventStartDateTime = view.findViewById(R.id.editTextStartDateTime);
        eventEndDateTime = view.findViewById(R.id.editTextEndDateTime);
        eventAddress = view.findViewById(R.id.editTextAddress);
        signUpButton = view.findViewById(R.id.sign_up_button);
        signUpMessage = view.findViewById(R.id.sign_up_message);
        backButton = view.findViewById(R.id.back_button);
        checkMarkImageView = view.findViewById(R.id.checkMarkImageView);
        progressBar = view.findViewById(R.id.loadingProgressBar);

        nameTextInputLayout = view.findViewById(R.id.textInputEventName);
        organizerTextInputLayout = view.findViewById(R.id.textInputOrganizer);
        descTextInputLayout = view.findViewById(R.id.textInputDescription);
        maxTextInputLayout = view.findViewById(R.id.textInputMaxAttendees);
        announceTextInputLayout = view.findViewById(R.id.textInputAnnouncements);
        startTextInputLayout = view.findViewById(R.id.textInputStartDateTime);
        endTextInputLayout = view.findViewById(R.id.textInputEndDateTime);
        addressTextInputLayout = view.findViewById(R.id.textInputAddress);

        progressBar.setVisibility(View.VISIBLE);

        signUpMessage.setText("You are signed up for this event!");
        signUpMessage.setVisibility(View.INVISIBLE);

        checkMarkImageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.coral));
        checkMarkImageView.setVisibility(View.INVISIBLE);
        checkMarkImageView.setColorFilter(ContextCompat.getColor(getContext(), R.color.coral));

        Log.d("position in detail", "position: " + position);

        Bundle bundle = this.getArguments();
        if (bundle != null) {
            eventId = bundle.getString("eventId");
            position = bundle.getInt("position");
            toMain = bundle.getBoolean("toMain");
        }
        androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseController.getInstance().isUserSignedUp(androidId, eventId, new FirebaseController.OnSignUpCheckListener() {
            @Override
            public void onSignUpCheck(boolean isSignedUp) {
                if (isSignedUp) {
                    signedUp = isSignedUp;
                    FirebaseController.getInstance().checkAttendeeCheckins(eventId, androidId, new FirebaseController.CheckAttendeeCheckinsCallback() {
                        @Override
                        public void onSuccess(int checkins) {
                            if (checkins == 1) {
                                signUpMessage.setText("Checked in 1 time!");
                                checkMarkImageView.setVisibility(View.VISIBLE);
                            } else if (checkins > 0) {
                                signUpMessage.setText("Checked in " + checkins + " times!");
                                checkMarkImageView.setVisibility(View.VISIBLE);

                            } else if (checkins == -1) {
                                signUpMessage.setText("You are signed-up to attend this event!");
                                checkedIn = false;
                            }
                            loadEventDetails(eventId);
                        }

                        @Override
                        public void onFailure(Exception e) {
                            Toast.makeText(getContext(), "Failed to get check-ins information.", Toast.LENGTH_SHORT).show();
                            checkedIn = false;
                            loadEventDetails(eventId);
                        }
                    });
                } else {
                    checkedIn = false;
                    loadEventDetails(eventId);
                }
            }
        });

        backButton.setOnClickListener(new View.OnClickListener() {
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

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseController.getInstance().getUser(androidId, new FirebaseController.OnUserRetrievedListener() {
                    @Override
                    public void onUserRetrieved(User user) {
                        if (user != null) {
                            FirebaseController.getInstance().getEvent(eventId, new FirebaseController.OnEventRetrievedListener() {
                                @Override
                                public void onEventRetrieved(Event event) {
                                    Log.d("EVENTID", "Number of attendees: " + eventId);
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

        // handle navigation to the organizers profile
        eventOrganizer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(requireContext(), ViewUserProfileActivity.class);
                intent.putExtra("userId", organizerId);
                startActivity(intent);
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
        builder.setTitle("Confirm sign-up for " + eventName + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        signUpButton.setVisibility(View.INVISIBLE);
                        signUpMessage.setVisibility(View.VISIBLE);
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // dismiss
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
                    organizerId = event.getOrganizer().getDeviceID();
                    displayEventDetails(event);
                } else {
                    Toast.makeText(requireContext(), "Failed to retrieve event details", Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.INVISIBLE);
                    eventPosterImage.setVisibility(View.VISIBLE);
                    eventName.setVisibility(View.VISIBLE);
                    eventOrganizer.setVisibility(View.VISIBLE);
                    eventDescription.setVisibility(View.VISIBLE);
                    eventMaxAttendees.setVisibility(View.VISIBLE);
                    eventAnnouncements.setVisibility(View.VISIBLE);
                    eventStartDateTime.setVisibility(View.VISIBLE);
                    eventEndDateTime.setVisibility(View.VISIBLE);
                    eventAddress.setVisibility(View.VISIBLE);

                    if (!signedUp) {
                        signUpButton.setVisibility(View.VISIBLE);
                    }
                    else {
                        signUpMessage.setVisibility(View.VISIBLE);
                    }
                    if (checkedIn) {
                        checkMarkImageView.setVisibility(View.VISIBLE);
                    }
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

        String eventNameText = event.getEventName() != null ? event.getEventName() : "N/A";
        if (eventNameText.length() > 16) {
            eventNameText = eventNameText.substring(0, 14) + "...";
        }
        eventName.setText(eventNameText);

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

        if (event.getAddress() != null) {
            eventAddress.setText(event.getAddress());
        } else {
            eventAddress.setText("N/A");
        }

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

        progressBar.setVisibility(View.INVISIBLE);
        eventPosterImage.setVisibility(View.VISIBLE);
        eventName.setVisibility(View.VISIBLE);
        eventOrganizer.setVisibility(View.VISIBLE);
        eventDescription.setVisibility(View.VISIBLE);
        eventMaxAttendees.setVisibility(View.VISIBLE);
        eventAnnouncements.setVisibility(View.VISIBLE);
        eventStartDateTime.setVisibility(View.VISIBLE);
        eventEndDateTime.setVisibility(View.VISIBLE);
        eventAddress.setVisibility(View.VISIBLE);

        nameTextInputLayout.setVisibility(View.VISIBLE);
        organizerTextInputLayout.setVisibility(View.VISIBLE);
        descTextInputLayout.setVisibility(View.VISIBLE);
        maxTextInputLayout.setVisibility(View.VISIBLE);
        announceTextInputLayout.setVisibility(View.VISIBLE);
        startTextInputLayout.setVisibility(View.VISIBLE);
        endTextInputLayout.setVisibility(View.VISIBLE);
        addressTextInputLayout.setVisibility(View.VISIBLE);

        if (!signedUp) {
            signUpButton.setVisibility(View.VISIBLE);
        }
        else {
            signUpMessage.setVisibility(View.VISIBLE);
        }
        if (checkedIn) {
            checkMarkImageView.setVisibility(View.VISIBLE);
        }
    }

    /**
     * returns the correct suffix for the given integer
     *
     * @param n the number in which to retrieve the suffix
     */
    public String getSuffix(Integer n) {
        if (n >= 11 && n <= 13) {
            return "th";
        } else {
            switch (n % 10) {
                case 1:
                    return "st";
                case 2:
                    return "nd";
                case 3:
                    return "rd";
                default:
                    return "th";
            }
        }
    }
}
