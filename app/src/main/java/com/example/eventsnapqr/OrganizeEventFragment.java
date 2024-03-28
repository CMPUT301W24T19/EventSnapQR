package com.example.eventsnapqr;

import static android.graphics.ImageDecoder.decodeBitmap;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.LevelListDrawable;
import android.Manifest;
import android.graphics.ImageDecoder;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.Binarizer;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.ChecksumException;
import com.google.zxing.FormatException;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;


import java.time.Instant;
import java.time.format.DateTimeFormatter;

import java.util.Calendar;
import java.util.Date;

/**
 * fragment where a user can organize an event using an eventName, an optional poster (default image
 * otherwise), a description and an optional max attendee. the option for reuse QR code is not yet
 * implemented
 */
public class OrganizeEventFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationClient;
    private ImageView backButton;
    private ExtendedFloatingActionButton createEventButton;
    private TextInputEditText editTextEventName;
    private TextInputEditText editTextEventDesc;
    private TextInputEditText editTextMaxAttendees;
    private TextInputEditText editTextStartDate;
    private TextInputEditText editTextStartTime;
    private TextInputEditText editTextEndDate;
    private TextInputEditText editTextEndTime;
    private TextInputEditText uploadPosterButton;
    private TextInputEditText locationButton;
    private TextInputLayout posterBox;
    private Button reuseQRButton;
    private String androidID;
    private FirebaseController firebaseController = new FirebaseController();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Uri imageUri;
    private String uriString;
    private String reusingQR;

    private double latitude = 0.0;
    private double longitude = 0.0;


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
        editTextEventDesc = view.findViewById(R.id.edit_text_number);
        editTextMaxAttendees = view.findViewById(R.id.editTextMaxAttendees);
        reuseQRButton = view.findViewById(R.id.buttonReuseQR);
        uploadPosterButton = view.findViewById(R.id.editTextPoster);
        posterBox = view.findViewById(R.id.posterInput);

        locationButton = view.findViewById(R.id.editTextLocation);

        // set up date and time picker dialogs
        editTextStartDate = view.findViewById(R.id.editTextStartDate);
        editTextStartDate.setOnClickListener(v -> showDatePickerDialog(editTextStartDate));

        editTextStartTime = view.findViewById(R.id.editTextStartTime);
        editTextStartTime.setOnClickListener(v -> showTimePickerDialog(editTextStartTime));

        editTextEndDate = view.findViewById(R.id.editTextEndDate);
        editTextEndDate.setOnClickListener(v -> showDatePickerDialog(editTextEndDate));

        editTextEndTime = view.findViewById(R.id.editTextEndTime);
        editTextEndTime.setOnClickListener(v -> showTimePickerDialog(editTextEndTime));
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestLocation();

        ActivityResultLauncher<PickVisualMediaRequest> choosePoster =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    if (uri != null) {
                        try {
                            InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                            Bitmap originalBitmap = BitmapFactory.decodeStream(inputStream);

                            // Resize the bitmap to a reasonable size
                            int targetWidth = 500;  // Adjust as needed
                            int targetHeight = 500; // Adjust as needed
                            Bitmap resizedBitmap = Bitmap.createScaledBitmap(originalBitmap, targetWidth, targetHeight, true);

                            // Create a drawable with the resized bitmap
                            Drawable drawable = new BitmapDrawable(getResources(), resizedBitmap);

// Set the bounds of the drawable to match the size of the TextInputEditText
                            drawable.setBounds(0, 0, uploadPosterButton.getWidth(), uploadPosterButton.getHeight());

// Set the background drawable of the TextInputEditText instead of the TextInputLayout
                            uploadPosterButton.setBackground(drawable);


                            // Set the background drawable of the TextInputLayout
                            posterBox.setStartIconDrawable(null); // Remove the drawable image
                            posterBox.setHint(null); // Remove the hint

                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    } else {
                        Log.d("TAG", "No media selected");
                    }
                });





        /**
         * Credits: https://stackoverflow.com/questions/55427308/scaning-qrcode-from-image-not-from-camera-using-zxing
         */
        ActivityResultLauncher<PickVisualMediaRequest> reuseQR =
                registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
                    // Callback is invoked after the user selects a media item or closes the
                    // photo picker.
                    if (uri != null) {
                        Log.d("TAG", "Selected URI: " + uri);
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                            ImageDecoder.Source qrBitMapDecoder = ImageDecoder.createSource(getActivity().getContentResolver(), uri);
                            Bitmap qrBitmap;
                            try {
                                qrBitmap = decodeBitmap(qrBitMapDecoder);
                                qrBitmap = qrBitmap.copy(Bitmap.Config.ARGB_8888, true);
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                            int[] pixels = new int[qrBitmap.getWidth() * qrBitmap.getHeight()];
                            qrBitmap.getPixels(pixels, 0, qrBitmap.getWidth(), 0, 0, qrBitmap.getWidth(), qrBitmap.getHeight());
                            LuminanceSource luminanceSource = new RGBLuminanceSource(qrBitmap.getWidth(), qrBitmap.getHeight(), pixels);
                            QRCodeReader reader = new QRCodeReader();
                            HybridBinarizer binarizer = new HybridBinarizer(luminanceSource);
                            Result decode;
                            try {
                                decode = reader.decode(new BinaryBitmap(binarizer));
                            } catch (Exception e) {
                                decode = null;
                            }
                            if (decode == null) {
                                Toast.makeText(getContext(), "Invalid QR code", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Result finalDecode = decode;
                                FirebaseController.getInstance().getEvent(decode.getText(), new FirebaseController.OnEventRetrievedListener() {
                                    @Override
                                    public void onEventRetrieved(Event event) {
                                        if (event == null) {
                                            Toast.makeText(getContext(), "QR code not generated by this app", Toast.LENGTH_SHORT).show();
                                        } else if (event.isActive()) {
                                            Log.d("TAG", "There is an event using this QR code");
                                            Toast.makeText(getContext(), "QR code currently in use", Toast.LENGTH_SHORT).show();
                                        }
                                        else {
                                            if (!event.getOrganizer().getDeviceID().equals(androidID)) {
                                                Log.d("TAG", "Organizer: " + event.getOrganizer().getDeviceID() + " Android ID: " + androidID);
                                                Toast.makeText(getContext(), "This is not your QR co    de, you cannot use it", Toast.LENGTH_SHORT).show();
                                            }
                                            else {
                                                Log.d("TAG", "QR code applied");
                                                reusingQR = finalDecode.getText();
                                                Toast.makeText(getContext(), "QR code successfully applied", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                            }
                        }
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
                choosePoster.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });

        reuseQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reuseQR.launch(new PickVisualMediaRequest.Builder()
                        .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                        .build());
            }
        });
        return view;
    }
    private void requestLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(requireActivity(), location -> {
                    if (location != null) {
                        // Logic to handle location object
                        latitude = location.getLatitude();
                        longitude = location.getLongitude();
                    }
                });
    }


    /**
     * validate each input field before creating an event
     * @return
     */
    private boolean validateInput() {
        String eventName = editTextEventName.getText().toString().trim();
        String eventDesc = editTextEventDesc.getText().toString().trim();
        String eventStartTime = editTextStartTime.getText().toString().trim();
        String eventStartDate = editTextStartDate.getText().toString().trim();
        String eventEndTime = editTextEndTime.getText().toString().trim();
        String eventEndDate = editTextEndDate.getText().toString().trim();

        if (eventName.isEmpty()) {
            editTextEventName.setError("Event Name Required");
            return false;
        }

        if (eventName.length() > 50) {
            editTextEventName.setError("Event name cannot exceed 50 characters");
            return false;
        }

        if (eventDesc.isEmpty()) {
            editTextEventDesc.setError("Event Description Required");
            return false;
        }

        if (eventStartDate.isEmpty()) {
            editTextStartDate.setError("Start Date Required");
            return false;
        }

        if (eventStartTime.isEmpty()) {
            editTextStartTime.setError("Start Time Required");
            return false;
        }

        if (eventEndDate.isEmpty()) {
            editTextEndDate.setError("End Date Required");
            return false;
        }

        if (eventEndTime.isEmpty()) {
            editTextEndTime.setError("End Time Required");
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
        //DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy hh:mm");
        DateFormat dateFormat = new SimpleDateFormat("dd/mm/yyyy");
        try {

            //startDateTime = dateFormat.parse(eventStartDate + " " +eventStartTime);
            startDateTime = dateFormat.parse(eventStartDate);
            //endDateTime = dateFormat.parse(eventEndDate + " " + eventEndTime);
            endDateTime = dateFormat.parse(eventEndDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        Integer eventMaxAttendees = !maxAttendeesInput.isEmpty() ? Integer.valueOf(maxAttendeesInput) : null;

        // retrieve user from the database based on the androidID, create a new user and event object
        FirebaseController.getInstance().getUser(androidID, new FirebaseController.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(User user) {
                if (user != null) {
                    String eventID;
                    if (reusingQR == null) {
                        eventID = FirebaseController.getInstance().getUniqueEventID();
                    }
                    else {
                        eventID = reusingQR;
                    }
                    Bundle bundle = new Bundle();
                    bundle.putString("eventId", eventID);

                    if (imageUri != null) {
                        StorageReference userRef = storageRef.child("eventPosters/" + eventID); // specifies the path on the cloud storage
                        userRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                            userRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                imageUri = uri;
                                uriString = imageUri.toString();
                                Event newEvent = new Event(user, eventName, eventDesc, uriString, eventMaxAttendees, eventID, startDateTime, endDateTime, true, latitude, longitude);
                                Log.d("USER NAME", newEvent.getOrganizer().getName());
                                firebaseController.addEvent(newEvent);
                                bundle.putString("destination", "main");
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    firebaseController.addMilestone(newEvent, "Event: " + newEvent.getEventName() + " created at: "+Instant.now());
                                }
                                else{
                                    firebaseController.addMilestone(newEvent, "Event: " + newEvent.getEventName() + "has been created");
                                }
                                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                                navController.navigate(R.id.action_organizeEventFragment_to_qRDialogFragment, bundle);
                            });
                        });
                    } else {
                        uriString = null;
                        Event newEvent = new Event(user, eventName, eventDesc, uriString, eventMaxAttendees, eventID, startDateTime, endDateTime, true, latitude, longitude);
                        newEvent.setLatitude(latitude);
                        newEvent.setLongitude(longitude);
                        Log.d("USER NAME", " "+newEvent.getOrganizer().getName());

                        firebaseController.addEvent(newEvent);
                        bundle.putString("destination", "main");
                        firebaseController.addOrganizedEvent(user, newEvent);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            firebaseController.addMilestone(newEvent, "Event: " + newEvent.getEventName() + " created at: "+Instant.now());
                        }
                        else{
                            firebaseController.addMilestone(newEvent, "Event: " + newEvent.getEventName() + "has been created");
                        }
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