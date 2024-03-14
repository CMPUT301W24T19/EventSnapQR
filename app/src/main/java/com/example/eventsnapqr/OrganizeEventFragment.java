package com.example.eventsnapqr;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.type.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;

/**
 * fragment where a user can organize an event using an eventName, an optional poster (default image
 * otherwise), a description and an optional max attendee. the option for reuse QR code is not yet
 * implemented
 */
public class OrganizeEventFragment extends Fragment {
    private ImageView backButton;
    private ExtendedFloatingActionButton createEventButton;
    private TextInputEditText editTextEventName;
    private TextInputEditText editTextEventDesc;
    private TextInputEditText editTextMaxAttendees;
    private TextInputEditText editTextStartDate;
    private TextInputEditText editTextStartTime;
    private TextInputEditText editTextEndDate;
    private TextInputEditText editTextEndTime;
    private Button uploadPosterButton;
    private String androidID;
    private FirebaseController firebaseController = new FirebaseController();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Uri imageUri;
    private String uriString;

    /**
     * Setup actions to be taken upon view creation and when the views are interacted with
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return the final view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organize_event, container, false);

        androidID = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        backButton = view.findViewById(R.id.button_back_button);
        createEventButton = view.findViewById(R.id.extendedFabCreateEvent);
        editTextEventName = view.findViewById(R.id.editTextEventName);
        editTextEventDesc = view.findViewById(R.id.editTextEventDesc);
        editTextMaxAttendees = view.findViewById(R.id.editTextMaxAttendees);
        uploadPosterButton = view.findViewById(R.id.buttonUploadPoster);

        // set up date and time picker dialogs
        editTextStartDate = view.findViewById(R.id.editTextStartDate);
        editTextStartDate.setOnClickListener(v -> showDatePickerDialog(editTextStartDate));

        editTextStartTime = view.findViewById(R.id.editTextStartTime);
        editTextStartTime.setOnClickListener(v -> showTimePickerDialog(editTextStartTime));

        editTextEndDate = view.findViewById(R.id.editTextEndDate);
        editTextEndDate.setOnClickListener(v -> showDatePickerDialog(editTextEndDate));

        editTextEndTime = view.findViewById(R.id.editTextEndTime);
        editTextEndTime.setOnClickListener(v -> showTimePickerDialog(editTextEndTime));

        ActivityResultLauncher<PickVisualMediaRequest> pickMedia =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("TAG", "Selected URI: " + uri);
                        imageUri = uri;
                    } else {
                        Log.d("TAG", "No media selected");
                    }
                });

        backButton.setOnClickListener(v -> navigateToMainPageFragment());
        createEventButton.setOnClickListener(v -> {
            if (validateInput()) {
                createEvent();
            }
        });

        uploadPosterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickMedia.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
        return view;
    }

    /**
     * validate each input field before creating an event
     * @return
     */
    private boolean validateInput() {
        String eventName = editTextEventName.getText().toString().trim();
        String eventDesc = editTextEventDesc.getText().toString().trim();

        if (eventName.isEmpty()) {
            editTextEventName.setError("Event name cannot be empty");
            return false;
        }

        if (eventName.length() > 50) {
            editTextEventName.setError("Event name cannot exceed 50 characters");
            return false;
        }

        if (eventDesc.isEmpty()) {
            editTextEventDesc.setError("Event description cannot be empty");
            return false;
        }
        return true;
    }

    /**
     * used to return to the main page
     */
    private void navigateToMainPageFragment() {
        getActivity().finish();
    }

    /**
     * add the event to the database and generate a unique QR code for the event
     */
    private void createEvent() {
        String eventName = editTextEventName.getText().toString(); // get the name of the event
        String eventDesc = editTextEventDesc.getText().toString(); // get the description of the event
        String maxAttendeesInput = editTextMaxAttendees.getText().toString();

        String eventStartDate = editTextStartDate.getText().toString();
        String eventStartTime = editTextStartTime.getText().toString();
        String eventEndDate = editTextEndDate.getText().toString();
        String eventEndTime = editTextEndTime.getText().toString();

        Date startDateTime;
        Date endDateTime;
        DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy hh:mm");
        try {
            startDateTime = dateFormat.parse(eventStartDate + " " +eventStartTime);
            endDateTime = dateFormat.parse(eventEndDate + " " + eventEndTime);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Integer eventMaxAttendees = !maxAttendeesInput.isEmpty() ? Integer.valueOf(maxAttendeesInput) : null;

        // retrieve user from the database based on the androidID, create a new user and event object
        FirebaseController.getInstance().getUser(androidID, new FirebaseController.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(User user) {
                if (user != null) {
                    String eventID = FirebaseController.getInstance().getUniqueEventID();
                    Bundle bundle = new Bundle();
                    bundle.putString("eventId", eventID);

                    if (imageUri != null) {
                        StorageReference userRef = storageRef.child("eventPosters/" + eventID); // specifies the path on the cloud storage
                        userRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                            userRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                imageUri = uri;
                                uriString = imageUri.toString();
                                Event newEvent = new Event(user, eventName, eventDesc, uriString, eventMaxAttendees, eventID, startDateTime, endDateTime);
                                Log.d("USER NAME", newEvent.getOrganizer().getName());
                                firebaseController.addEvent(newEvent);
                                bundle.putString("destination", "main");
                                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                                navController.navigate(R.id.action_organizeEventFragment_to_qRDialogFragment, bundle);
                            });
                        });
                    } else {
                        uriString = null;
                        Event newEvent = new Event(user, eventName, eventDesc, uriString, eventMaxAttendees, eventID, startDateTime, endDateTime);
                        Log.d("USER NAME", newEvent.getOrganizer().getName());
                        firebaseController.addEvent(newEvent);
                        firebaseController.addOrganizedEvent(user, newEvent);
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                        navController.navigate(R.id.action_organizeEventFragment_to_qRDialogFragment, bundle);
                    }
                }
            }
        });
    }

    private void showDatePickerDialog(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                requireContext(),
                (view, selectedYear, selectedMonth, selectedDayOfMonth) -> {
                    String selectedDate = selectedDayOfMonth + "/" + (selectedMonth + 1) + "/" + selectedYear;
                    editText.setText(selectedDate);
                },
                year, month, dayOfMonth);
        datePickerDialog.show();
    }

    private void showTimePickerDialog(TextInputEditText editText) {
        Calendar calendar = Calendar.getInstance();
        int hourOfDay = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(
                requireContext(),
                (view, selectedHourOfDay, selectedMinute) -> {
                    String selectedTime = selectedHourOfDay + ":" + selectedMinute;
                    editText.setText(selectedTime);
                },
                hourOfDay, minute, false);
        timePickerDialog.show();
    }
}