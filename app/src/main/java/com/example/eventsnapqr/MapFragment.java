package com.example.eventsnapqr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

/**
 * fragment where the map is plotted for the event clicked
 * the following sources were a major help  for setting this up
 *
 * I used OpenAI: chatGPT to get the structure of how to plot the coordinates
 * using osmdroid. Prompt "How can I plot a set of coordinated using osmdroid."
 * The startLocationUpdates, onProvider, onLocation change, onResume, onPause
 * permission check and request is taken from chatgpt.
 *
 * Along with that I used this video to get run time permissions:
 * "https://www.youtube.com/watch?v=KeuV6cjVh6c"
 *
 * To get current location I also referred to these 3 videos:
 * "https://www.youtube.com/watch?v=M0kUd2dpxo4"
 * "https://www.youtube.com/watch?v=waX6ygjIqmw"
 * Used code to setup the manView on the xml and initialization from the video below
 * "https://www.youtube.com/watch?v=xoFtgcOoO1I"
 */
public class MapFragment extends Fragment {

    private MapView mapView;
    private FirebaseFirestore db;
    private String eventName;
    private FrameLayout mapContainer;

    //constructor to get the passes EventName
    public MapFragment(String eventName) {
        this.eventName = eventName;
        //plotEventAttendees(eventName);
    }



    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Initialize OSMDroid configuration
        Configuration.getInstance().load(getContext(), getActivity().getSharedPreferences("osmdroid", 0));
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {

            eventName = getArguments().getString("eventName");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendee_map, container, false);
        TextView text = view.findViewById(R.id.page_name);
        text.setText("Map of " + eventName + " Attendees");

        mapView = view.findViewById(R.id.mapView);
        mapContainer = view.findViewById(R.id.mapContainer); // Reference to the parent layout
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        Log.e("MapFragment", "Map clicked. Plotting attendees for event: " + eventName);
        plotEventAttendees(eventName);

        // Request location permission
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Permission granted, start requesting location updates
            startLocationUpdates();
        } else {
            // Permission not granted, request it
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        // Set up the back button click listener
        view.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e("MapFragment", "Back clicked. Plotting attendees for event: " + eventName);
                requireActivity().onBackPressed();
            }
        });
        mapContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // When parent layout is clicked, plot attendees of the event
                Log.e("MapFragment", "Map clicked. Plotting attendees for event: " + eventName);
                Log.e("MapFragment", "Reached method call");
                plotEventAttendees(eventName);
            }
        });

        return view;
    }


    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mapView.invalidate(); // Refresh the map view
            }

            @Override
            public void onProviderDisabled(String provider) {
            }

            @Override
            public void onProviderEnabled(String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };

        try {
            // Request location updates
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void plotEventAttendees(String eventName) {
        // Query Firestore to find the event document based on the event name
        db.collection("events")
                .whereEqualTo("eventName", eventName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            // Get the event document ID
                            String eventId = document.getId();

                            // Query the "attendees" subcollection to get the list of attendees
                            db.collection("events").document(eventId)
                                    .collection("attendees")
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                // Extract latitude and longitude values as strings
                                                String latitudeStr = document.getString("latitude");
                                                String longitudeStr = document.getString("longitude");
                                                Log.e("MapFragment", "Found latitude " + latitudeStr);
                                                Long checkINLong = document.getLong("checkedIn");
                                                Log.e("MapFragment", "Found checkIN:  " + checkINLong);

                                                if (checkINLong != null && latitudeStr!=null && longitudeStr!=null) {
                                                    int checkIN = checkINLong.intValue();

                                                    // Convert latitude and longitude from strings to doubles
                                                    double latitude = Double.parseDouble(latitudeStr);
                                                    double longitude = Double.parseDouble(longitudeStr);
                                                    Log.e("MapFragment", "Found latitude " + latitude);

                                                    if(checkIN>0) {
                                                        Marker marker = new Marker(mapView);
                                                        marker.setPosition(new GeoPoint(latitude, longitude));
                                                        // Getting the attendee ID
                                                        String attendeeId = document.getId();

                                                        // Retrieve user information using the attendee ID
                                                        db.collection("users").document(attendeeId)
                                                                .get()
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                        if (documentSnapshot.exists()) {
                                                                            // Retrieve the user name
                                                                            String userName = documentSnapshot.getString("name");
                                                                            // Set marker title with user name
                                                                            marker.setTitle("User name: " + userName);
                                                                        } else {
                                                                            Log.e("MapFragment", "User document does not exist for attendee ID: " + attendeeId);
                                                                        }
                                                                    }
                                                                })
                                                                .addOnFailureListener(new OnFailureListener() {
                                                                    @Override
                                                                    public void onFailure(@NonNull Exception e) {
                                                                        Log.e("MapFragment", "Error getting user document: " + e.getMessage());
                                                                    }
                                                                });

                                                        marker.setSnippet("Checked In: " + checkIN); // Include the check-in information in the snippet
                                                        mapView.getOverlays().add(marker);
                                                    }
                                                }
                                            }
                                            mapView.invalidate(); // Refresh the map view
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("MapFragment", "Error getting attendees: " + e.getMessage());
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("MapFragment", "Error getting event document: " + e.getMessage());
                    }
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }
}