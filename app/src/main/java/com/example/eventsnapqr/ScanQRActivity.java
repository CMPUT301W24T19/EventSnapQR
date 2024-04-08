package com.example.eventsnapqr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.Manifest;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.core.app.ActivityCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * activity where the QR scanner is implemented using ZXing library
 * the following video was a major help and source for setting up
 * this functionality
 *
 * https://www.youtube.com/watch?v=bWEt-_z7BOY&ab_channel=EasyOneCoders
 * the view event details after scanning an event you have not signed up
 * for is not currently linked to the event page. planned for project part 4
 *
 * I used OpenAI: chatGPT to get the structure of how to get my coordinates
 * using Location manager. Prompt "How can I get my coordinates using Location
 * Manager in Android studios." The onProvider, onStatuschange, onLocationChange,
 * permission check and request is taken from chatgpt.
 *
 * Along with that I used this video to get run time permissions:
 * "https://www.youtube.com/watch?v=KeuV6cjVh6c"
 *
 * To get current location I also referred to these 3 videos:
 * "https://www.youtube.com/watch?v=M0kUd2dpxo4"
 * "https://www.youtube.com/watch?v=waX6ygjIqmw"
 * "https://www.youtube.com/watch?v=xoFtgcOoO1I"
 */
public class ScanQRActivity extends AppCompatActivity {
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private String userId;
    private String eventId;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private double latitudeNow;
    private double longitudeNow;
    private TextView qrMessageTextView;
    private Button miscButton;
    private ExtendedFloatingActionButton scanButton;
    private ImageView backButton;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final int PERMISSION_REQUEST_LOCATION = 2;
    private ProgressBar progressBar;

    /**
     * What should be executed when the fragment is created
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);
        userId = Settings.Secure.getString(this.getContentResolver(), Settings.Secure.ANDROID_ID);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        qrMessageTextView = findViewById(R.id.qrMessage);
        miscButton = findViewById(R.id.miscButton);
        backButton = findViewById(R.id.button_back);
        scanButton = findViewById(R.id.scan_qr_button);
        progressBar = findViewById(R.id.loadingProgressBar);

        backButton.setOnClickListener(view -> finish());

        scanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                Log.d("TAG", "true");
                finish();
                startActivity(intent);
            }
        });

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                latitudeNow = location.getLatitude();
                longitudeNow = location.getLongitude();
                Log.d("ScanQRActivity", "Latitude: " + latitudeNow + ", Longitude: " + longitudeNow);
            }

            @Override
            public void onProviderDisabled(String provider) {
                Log.e("ScanQRActivity", "Provider disabled: " + provider);
            }

            @Override
            public void onProviderEnabled(String provider) {
                Log.d("ScanQRActivity", "Provider enabled: " + provider);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
                Log.d("ScanQRActivity", "Provider status changed: " + provider);
            }
        };

        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_FINE); // Use fine accuracy for better location
        criteria.setPowerRequirement(Criteria.POWER_LOW);
        String provider = locationManager.getBestProvider(criteria, true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.e("ScanQR Activity", "Location is not enabled");
        } else {
            if (provider != null) {
                locationManager.requestLocationUpdates(provider, 1000, 10, locationListener); // Update every 1 second or 10 meters
            } else {
                Log.e("ScanQRActivity", "No suitable provider found");
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, PERMISSION_REQUEST_CAMERA);
            } else {
                initQRCodeScanner();
            }
        } else {
            initQRCodeScanner();
        }
    }

    /**
     * Method to get the latitude and longitude.
     */
    private void getLatitudeAndLongitude() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation != null) {
                latitudeNow = lastKnownLocation.getLatitude();
                longitudeNow = lastKnownLocation.getLongitude();
                Log.d("ScanQRActivity", "Latitude: " + latitudeNow + ", Longitude: " + longitudeNow);
            } else {
                // Handle case when last known location is not available
                Log.e("ScanQRActivity", "Last known location is not available.");
            }
        } else {
            // Permission not granted, request it
            //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            Log.e("Scan QR ", "Location services (GPS) are not enabled");
        }
    }

    /**
     * integrate the qr scanner once permissions are verified
     */
    private void initQRCodeScanner() {
        // https://stackoverflow.com/questions/34983201/change-qr-scanner-orientation-with-zxing-in-android-studio
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
        integrator.setOrientationLocked(false);
        integrator.setPrompt("Scan QR Code");
        integrator.setCameraId(0);
        integrator.initiateScan();
    }


    /**
     * update the user if they have successfully checked into the event
     *
     * @param eventId the unique identifier of the given event
     */
    private void checkIn(String eventId) {
        FirebaseController.getInstance().incrementCheckIn(userId, eventId, new FirebaseController.CheckInListener() {
            @Override
            public void onCheckInComplete(int count) {
                FirebaseController.getInstance().getEvent(eventId, new FirebaseController.OnEventRetrievedListener() {
                    @Override
                    public void onEventRetrieved(Event event) {
                        if (event != null) {
                            // Call getLatitudeAndLongitude() to update latitude and longitude variables
                            getLatitudeAndLongitude();
                            DocumentReference eventRef = db.collection("events").document(eventId)
                                    .collection("attendees").document(userId);
                            String lat = String.valueOf(latitudeNow);
                            String longi = String.valueOf(longitudeNow);

                            Map<String, Object> updates = new HashMap<>();
                            updates.put("latitude", lat); // Setting latitude to the longitude value
                            updates.put("longitude", longi);

                            eventRef.update(updates)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("Success", "latitude works" + latitudeNow);
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.d("fail", "latitude fail");
                                        }
                                    });

                            getLatitudeAndLongitude();
                            // Use latitude and longitude variables here
                            Log.d("ScanQRActivity", "Latitude: " + latitudeNow + ", Longitude: " + longitudeNow);

                            qrMessageTextView.setText("Checked into " + event.getEventName() + " for the " + count + getSuffix(count) + " time!");
                            qrMessageTextView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);

                        } else {
                            qrMessageTextView.setText("Failed to retrieve event details.");
                            qrMessageTextView.setVisibility(View.VISIBLE);
                            progressBar.setVisibility(View.GONE);
                        }
                    }
                });
            }

            @Override
            public void onCheckInFailure(Exception e) {
                qrMessageTextView.setText("Failed to increment check-in count.");
            }
        });
    }

    /**
     * handles what to do with the content of the QR code
     *
     * @param requestCode The integer request code originally supplied to
     *                    startActivityForResult(), allowing you to identify who this
     *                    result came from.
     * @param resultCode  The integer result code returned by the child activity
     *                    through its setResult().
     * @param data        An Intent, which can return result data to the caller
     *                    (various data can be attached to Intent "extras").
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        progressBar.setVisibility(View.VISIBLE);
        qrMessageTextView.setVisibility(View.INVISIBLE);
        IntentResult intentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (intentResult != null) {
            String contents = intentResult.getContents();
            if (contents != null) {

                Log.d("Scan QR Activity", "QR code content: " + contents); // Log the content of the QR code
                FirebaseController.getInstance().getAllEvents(new FirebaseController.OnEventsLoadedListener() {
                    @Override
                    public void onEventsLoaded(ArrayList<Event> events) {
                        for (Event event: events) {
                            Log.d("TAG", "Event QR: " + event.getQR());
                            if (contents.equals(event.getQR())) {
                                eventId = event.getEventID();
                                //Log.d("TAG", "true");
                                Log.d("TAG", "Event ID: " + event.getEventID());
                            }
                        }
                        if (eventId != null) { // if a valid qr is scanned
                            FirebaseController.getInstance().checkUserInAttendees(eventId, userId, new FirebaseController.OnUserInAttendeesListener() {
                                @Override
                                public void onUserInAttendees(boolean isInAttendees) {
                                    if (isInAttendees) {
                                        Log.d("Scan QR Activity", "User is in attendees");
                                        checkIn(eventId);
                                    } else {
                                        Log.d("Scan QR Activity", "User is not in attendees");
                                        notSignedUp(eventId);
                                    }
                                }

                                @Override
                                public void onCheckFailed(Exception e) {
                                    Log.e("Scan QR Activity", "User in Event attendees check failed: " + e.getMessage());
                                    nonExistentEvent();
                                }
                            });
                        } else { // case when no QR code was scanned before camera closed
                            nonExistentEvent();
                        }
                    }
                });
            } else {
                progressBar.setVisibility(View.GONE);
                qrMessageTextView.setVisibility(View.VISIBLE);
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    /**
     * if the user has not signed up scanned event, give them the option to view the event details
     * or to return to the main page
     *
     * @param eventId identifier of the given event
     */
    private void notSignedUp(String eventId) {
        qrMessageTextView.setText("Not signed up for this event.");
        qrMessageTextView.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        miscButton.setText("View Event Details");
        miscButton.setVisibility(View.VISIBLE);
        miscButton.setOnClickListener(view -> {
            Intent intent = new Intent(ScanQRActivity.this, BrowseEventsActivity.class);
            intent.putExtra("eventID", eventId);
            startActivity(intent);
        });

        backButton.setOnClickListener(view -> finish());
    }

    /**
     * handles case where a qrcode is scanned, but not generated by EventSnapQR
     */
    private void nonExistentEvent() {
        if (!isFinishing()) {
            qrMessageTextView.setText("No active event with QR code");
            qrMessageTextView.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            backButton.setOnClickListener(view -> finish());
        }
    }

    /**
     * returns the correct suffix for the given integer
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