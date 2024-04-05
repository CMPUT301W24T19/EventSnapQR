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
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

public class MapFragmentOrganize extends Fragment {

    private MapView mapView;
    private FirebaseFirestore db;
    private String eventName;
    private FrameLayout mapContainer;

    // Added for receiving latitude and longitude
    private double targetLatitude = 0.0;
    private double targetLongitude = 0.0;

    public MapFragmentOrganize() {
        // Required empty public constructor
    }
    public MapFragmentOrganize(String eventName) {
        this.eventName = eventName;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getContext(), getActivity().getSharedPreferences("osmdroid", 0));
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            eventName = getArguments().getString("eventName");

            // Receive latitude and longitude if provided
            targetLatitude = getArguments().getDouble("latitude", 0.0);
            targetLongitude = getArguments().getDouble("longitude", 0.0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendee_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapContainer = view.findViewById(R.id.mapContainer);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(16);

        // Check if coordinates are provided and valid
        if (targetLatitude != 0.0 && targetLongitude != 0.0) {
            // Center map on provided coordinates
            GeoPoint startPoint = new GeoPoint(targetLatitude, targetLongitude);
            mapController.setCenter(startPoint);
            Marker startMarker = new Marker(mapView);
            startMarker.setPosition(startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(startMarker);
        } else {
            // Fallback behavior: plot attendees for event if no specific coordinates provided
            Log.e("MapFragment", "Map clicked. Plotting attendees for event: " + eventName);
            plotEventAttendees(eventName);
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        view.findViewById(R.id.button_back_button).setOnClickListener(v -> requireActivity().onBackPressed());

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
                                            List<GeoPoint> points = new ArrayList<>();
                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                // Extract latitude and longitude values as strings
                                                String latitudeStr = document.getString("latitude");
                                                String longitudeStr = document.getString("longitude");
                                                Log.e("MapFragment", "Found latitude " + latitudeStr);
                                                Long checkINLong = document.getLong("checkedIn");
                                                Log.e("MapFragment", "Found checkIN:  " + checkINLong);

                                                if (checkINLong != null && !latitudeStr.isEmpty() && latitudeStr!=null && !longitudeStr.isEmpty() && longitudeStr!=null) {
                                                    int checkIN = checkINLong.intValue();

                                                    // Convert latitude and longitude from strings to doubles

                                                    double latitude = Double.parseDouble(latitudeStr);
                                                    double longitude = Double.parseDouble(longitudeStr);
                                                    Log.e("MapFragment", "Found latitude " + latitude);

                                                    if(checkIN>0) {
                                                        points.add(new GeoPoint(latitude, longitude));
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
                                            if (!points.isEmpty()) {
                                                // update the map view to include all markers
                                                if (points.size() == 1) {
                                                    // if there's only one point, center the map on it
                                                    mapView.getController().setCenter(points.get(0));
                                                } else {
                                                    // calculate the bounding box and set the map view
                                                    BoundingBox boundingBox = BoundingBox.fromGeoPoints(points);
                                                    mapView.zoomToBoundingBox(boundingBox, true);
                                                }
                                            }
                                            else{
                                                Log.d("ORGANIZER MAP", "No points to focus map");
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