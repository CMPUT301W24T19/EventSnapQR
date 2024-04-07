package com.example.eventsnapqr;

import static android.graphics.ImageDecoder.decodeBitmap;
import static androidx.camera.core.impl.utils.ContextUtil.getApplicationContext;

import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationCallback;
import android.os.Looper;

import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.FusedLocationProviderClient;
import android.location.Geocoder;
import android.location.Address;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Matrix;
import android.graphics.Shader;
import android.Manifest;
import android.graphics.ImageDecoder;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;


import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * fragment where a user can organize an event using an eventName, an optional poster (default image
 * otherwise), a description and an optional max attendee. the option for reuse QR code is not yet
 * implemented
 */
public class OrganizeEventFragment extends Fragment {
    private FusedLocationProviderClient fusedLocationClient;
    private ImageView backButton, imageViewPoster;
    private ExtendedFloatingActionButton createEventButton;
    private TextInputEditText editTextEventName, editTextEventDesc, editTextMaxAttendees, editTextStartDate,
            editTextStartTime, editTextEndDate, editTextEndTime, editTextAddress, editTextLocation;
    private TextInputLayout inputTextLocation;
    private TextView removePosterTextView, uploadPosterHint;
    private CardView cardViewPoster;
    private MaterialButton reuseQRButton;
    private String androidID, uriString, reusingQR;
    private FirebaseController firebaseController = new FirebaseController();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Uri imageUri;
    private double latitude = 0.0;
    private double longitude = 0.0;
    private boolean eventCreated = false;
    private boolean getLocation = false;


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
        editTextEventDesc = view.findViewById(R.id.editTextDescription);
        editTextMaxAttendees = view.findViewById(R.id.editTextMaxAttendees);

        editTextLocation = view.findViewById(R.id.editTextLocation);
        inputTextLocation = view.findViewById(R.id.inputTextLocation);

        reuseQRButton = view.findViewById(R.id.buttonReuseQR);
        cardViewPoster = view.findViewById(R.id.posterCardView);
        imageViewPoster = view.findViewById(R.id.imageViewPoster);
        removePosterTextView = view.findViewById(R.id.removePosterTextView);
        uploadPosterHint = view.findViewById(R.id.textViewHint);

        editTextStartDate = view.findViewById(R.id.editTextStartDate);
        editTextStartDate.setOnClickListener(v -> showDatePickerDialog(editTextStartDate));

        editTextStartTime = view.findViewById(R.id.editTextStartTime);
        editTextStartTime.setOnClickListener(v -> showTimePickerDialog(editTextStartTime));

        editTextEndDate = view.findViewById(R.id.editTextEndDate);
        editTextEndDate.setOnClickListener(v -> showDatePickerDialog(editTextEndDate));

        editTextEndTime = view.findViewById(R.id.editTextEndTime);
        editTextEndTime.setOnClickListener(v -> showTimePickerDialog(editTextEndTime));

        editTextAddress = view.findViewById(R.id.editTextAddress);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity());
        requestLocation();

        removePosterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                removePosterTextView.setVisibility(View.INVISIBLE);
                Drawable uploadIcon = ContextCompat.getDrawable(getContext(), android.R.drawable.ic_menu_upload);
                imageViewPoster.setImageDrawable(uploadIcon);
                uploadPosterHint.setText("Upload Poster");
                imageUri = null;
                uriString = null;
            }
        });




        ActivityResultLauncher<PickVisualMediaRequest> choosePoster = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                try {
                    uploadPosterHint.setText(null);
                    int targetW = imageViewPoster.getWidth();
                    int targetH = imageViewPoster.getHeight();

                    BitmapFactory.Options bmOptions = new BitmapFactory.Options();
                    bmOptions.inJustDecodeBounds = true;
                    InputStream inputStream = requireContext().getContentResolver().openInputStream(uri);
                    BitmapFactory.decodeStream(inputStream, null, bmOptions);
                    inputStream.close();

                    int photoW = bmOptions.outWidth;
                    int photoH = bmOptions.outHeight;

                    int scaleFactor = Math.max(1, Math.min(photoW/targetW, photoH/targetH));

                    bmOptions.inJustDecodeBounds = false;
                    bmOptions.inSampleSize = scaleFactor;
                    bmOptions.inPurgeable = true;

                    inputStream = requireContext().getContentResolver().openInputStream(uri);
                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, bmOptions);
                    inputStream.close();

                    imageViewPoster.post(() -> imageViewPoster.setImageBitmap(bitmap));

                    removePosterTextView.setVisibility(View.VISIBLE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageUri = uri;
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
                                if (!decode.getText().startsWith("eventsnapqr/")) {
                                    Toast.makeText(getContext(), "QR code not generated by this app", Toast.LENGTH_SHORT).show();
                                }
                                else {
                                    Log.d("TAG", "true");
                                    try {
                                        reusingQR = decode.getText().toString().split("/")[1];
                                        FirebaseController.getInstance().getEvent(reusingQR, new FirebaseController.OnEventRetrievedListener() {
                                            @Override
                                            public void onEventRetrieved(Event event) {
                                                if (event == null) {
                                                    Log.d("TAG", "TRUE2");
                                                    Toast.makeText(getContext(), "QR code not recognized by this app", Toast.LENGTH_SHORT).show();
                                                } else if (event.isActive()) {
                                                    Log.d("TAG", "There is an event using this QR code");
                                                    Toast.makeText(getContext(), "QR code currently in use", Toast.LENGTH_SHORT).show();
                                                } else {
                                                    if (!event.getOrganizer().getDeviceID().equals(androidID)) {
                                                        Log.d("TAG", "Organizer: " + event.getOrganizer().getDeviceID() + " Android ID: " + androidID);
                                                        Toast.makeText(getContext(), "This is not your QR co    de, you cannot use it", Toast.LENGTH_SHORT).show();
                                                    } else {
                                                        Log.d("TAG", "QR code applied");
                                                        Toast.makeText(getContext(), "QR code successfully applied", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                    catch (Exception e) {
                                        Toast.makeText(getContext(), "QR code retrieval failure", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                    }
                });

        backButton.setOnClickListener(v -> getActivity().finish());
        createEventButton.setOnClickListener(v -> {
            if (validateInput(view) && !eventCreated) {
                eventCreated = true;
                createEvent();
            }
        });

        cardViewPoster.setOnClickListener(new View.OnClickListener() {
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
        editTextLocation.setOnClickListener(v -> {
            if (!getLocation) {
                getLocation = true;
                requestCurrentLocation();
            }
        });
        return view;
    }
    /**
     * Requests continuous updates of the device's current location. If location permissions are not granted,
     * requests the user for the necessary permissions. Sets up a LocationRequest to define the desired
     * accuracy and frequency of location updates. Uses a LocationCallback to handle received location updates.
     * Upon receiving a location update, attempts to geocode the current address based on the location's latitude
     * and longitude, appending the city and country to the user's input from editTextLocation.
     * If a valid location is found, navigates to the map fragment with the geocoded location.
     */
    private void requestCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // Request permission
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 101);
            return;
        }

        LocationRequest locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(10000); // 10 seconds
        locationRequest.setFastestInterval(5000); // 5 seconds

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    Toast.makeText(getContext(), "Current location not available.", Toast.LENGTH_LONG).show();
                    getLocation = false;
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    if (location != null) {
                        // Stop location updates
                        fusedLocationClient.removeLocationUpdates(this);

                        // Now use the location to find the city, country, and then geocode the address
                        try {
                            Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
                            List<Address> currentAddresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                            if (!currentAddresses.isEmpty()) {
                                Address currentAddress = currentAddresses.get(0);
                                String city = currentAddress.getLocality();
                                String country = currentAddress.getCountryName();
                                String fullAddress = editTextLocation.getText().toString() + ", " + city + ", " + country;

                                List<Address> addresses = geocoder.getFromLocationName(fullAddress, 1);
                                if (addresses != null && !addresses.isEmpty()) {
                                    Address address = addresses.get(0);

                                    // Create a bundle and add the latitude and longitude
                                    Bundle bundle = new Bundle();
                                    bundle.putDouble("latitude", address.getLatitude());
                                    bundle.putDouble("longitude", address.getLongitude());

                                    NavController navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);
                                    navController.navigate(R.id.global_action_to_mapFragmentOrganize, bundle);
                                } else {
                                    Toast.makeText(getContext(), "Location not found. Please try a different address.", Toast.LENGTH_LONG).show();
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Geocoder failed, please try again later.", Toast.LENGTH_LONG).show();
                        }

                        break;
                    }
                }
            }
        };

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        getLocation = false;
    }
    /**
     * Requests the last known location of the device. If location permissions are not granted,
     * requests the user for the necessary permissions. Upon receiving the location, updates
     * the global latitude and longitude variables with the current location's coordinates.
     * This method is best used when a single, last-known location is sufficient for your needs.
     */
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
    public void updateLocationText(double latitude, double longitude) {
        String latString = Double.toString(latitude);
        String longString = Double.toString(longitude);
        Log.d(latString, longString);
        if (editTextLocation != null) {
            String locationText = String.format(Locale.getDefault(), "%.5f, %.5f", latitude, longitude);
            editTextLocation.setText(locationText);
        }
    }

    /**
     * validate each input field before creating an event
     * @return
     */
    private boolean validateInput(View view) {
        boolean isValid = true;

        String eventName = editTextEventName.getText().toString().trim();
        String eventDesc = editTextEventDesc.getText().toString().trim();
        String eventStartTime = editTextStartTime.getText().toString().trim();
        String eventStartDate = editTextStartDate.getText().toString().trim();
        String eventEndTime = editTextEndTime.getText().toString().trim();
        String eventEndDate = editTextEndDate.getText().toString().trim();
        String eventAddress = editTextAddress.getText().toString().trim();
        String maxAttendeesString = editTextMaxAttendees.getText().toString().trim();

        if (eventName.isEmpty()) {
            editTextEventName.setError("Event Name Required");
            isValid = false;
        }

        if (eventDesc.isEmpty()) {
            editTextEventDesc.setError("Event Description Required");
            isValid = false;
        }

        if (eventAddress.isEmpty()) {
            editTextAddress.setError("Event Address Required");
            isValid = false;
        }

        if (eventStartDate.isEmpty()) {
            editTextStartDate.setError("Start Date Required");
            isValid = false;
        }

        if (eventStartTime.isEmpty()) {
            editTextStartTime.setError("Start Time Required");
            isValid = false;
        }

        if (eventEndDate.isEmpty()) {
            editTextEndDate.setError("End Date Required");
            isValid = false;
        }

        if (eventEndTime.isEmpty()) {
            editTextEndTime.setError("End Time Required");
            isValid = false;
        }

        if (!maxAttendeesString.isEmpty()) {
            int maxAttendees = Integer.parseInt(maxAttendeesString);
            if (maxAttendees < 1) {
                editTextMaxAttendees.setError("Minimum 1 Attendee");
                isValid = false;
            }
        }

        return isValid;
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
        String eventAddress = editTextAddress.getText().toString();

        Date startDateTime;
        Date endDateTime;

        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy" + " hh:mm");
        try {
            startDateTime = dateFormat.parse(eventStartDate + " " + eventStartTime);
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
                    String eventID;
                    if (reusingQR == null) {
                        eventID = FirebaseController.getInstance().getUniqueEventID();
                    }
                    else {
                        eventID = reusingQR;
                    }

                    if (imageUri != null) {
                        StorageReference userRef = storageRef.child("eventPosters/" + eventID); // specifies the path on the cloud storage
                        userRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                            userRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                imageUri = uri;
                                uriString = imageUri.toString();
                                Event newEvent = new Event(user, eventName, eventDesc, uriString, eventMaxAttendees, eventID, startDateTime, endDateTime, eventAddress, true);
                                Log.d("USER NAME", newEvent.getOrganizer().getName());
                                firebaseController.addEvent(newEvent);
                                firebaseController.addOrganizedEvent(user, newEvent);
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                    LocalDateTime now = LocalDateTime.now();
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy '@' hh:mm a");
                                    String formattedNow = formatter.format(now);
                                    firebaseController.addMilestone(newEvent, newEvent.getEventName() + " created on "+ formattedNow);
                                }
                                else{
                                    firebaseController.addMilestone(newEvent, "Event: " + newEvent.getEventName() + "has been created");
                                }
                                Intent intent = new Intent(getActivity(), QRActivity.class);
                                intent.putExtra("eventId", eventID);
                                intent.putExtra("destination", "main");
                                startActivity(intent);
                                getActivity().finish();
                            });
                        });
                    } else {
                        uriString = null;
                        Event newEvent = new Event(user, eventName, eventDesc, uriString, eventMaxAttendees, eventID, startDateTime, endDateTime, eventAddress, true);
                        Log.d("USER NAME", " "+newEvent.getOrganizer().getName());

                        firebaseController.addEvent(newEvent);
                        firebaseController.addOrganizedEvent(user, newEvent);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            LocalDateTime now = LocalDateTime.now();
                            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy '@' hh:mm a");
                            String formattedNow = formatter.format(now);
                            firebaseController.addMilestone(newEvent, newEvent.getEventName() + " created on "+ formattedNow);
                        }
                        else{
                            firebaseController.addMilestone(newEvent, "Event: " + newEvent.getEventName() + "has been created");
                        }
                        Intent intent = new Intent(getActivity(), QRActivity.class);
                        intent.putExtra("eventId", eventID);
                        intent.putExtra("destination", "main");
                        startActivity(intent);
                        getActivity().finish();
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