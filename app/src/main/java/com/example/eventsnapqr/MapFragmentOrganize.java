package com.example.eventsnapqr;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
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
import org.osmdroid.views.Projection;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.Overlay;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MapFragmentOrganize extends Fragment {
    private OnLocationPickedListener listener;
    private MapView mapView;
    private FirebaseFirestore db;
    private String eventName;
    private FrameLayout mapContainer;

    // Added for receiving latitude and longitude
    private double targetLatitude = 0.0;
    private double targetLongitude = 0.0;
    private Marker lastMarker = null;
    private Marker initialMarker = null;

    public MapFragmentOrganize() {
        // Required empty public constructor
    }
    public MapFragmentOrganize(String eventName) {
        this.eventName = eventName;
    }
    public interface OnLocationPickedListener {
        void onLocationPicked(double latitude, double longitude);
    }
    private void notifyLocationPicked(double latitude, double longitude) {
        if (listener != null) {
            listener.onLocationPicked(latitude, longitude);
        }
    }
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = (OnLocationPickedListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnLocationPickedListener");
        }
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
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        mapContainer = view.findViewById(R.id.mapContainer);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(16);

        Bundle args = getArguments();
        if (args != null && args.containsKey("address")) {
            String addressStr = args.getString("address");
            new Thread(() -> {
                try {
                    Geocoder geocoder = new Geocoder(getContext());
                    List<Address> addresses = geocoder.getFromLocationName(addressStr, 1);
                    if (!addresses.isEmpty()) {
                        Address address = addresses.get(0);
                        double latitude = address.getLatitude();
                        double longitude = address.getLongitude();

                        // Update UI on UI thread
                        getActivity().runOnUiThread(() -> {
                            GeoPoint startPoint = new GeoPoint(latitude, longitude);
                            mapController.setCenter(startPoint);
                            Marker startMarker = new Marker(mapView);
                            startMarker.setPosition(startPoint);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            mapView.getOverlays().add(startMarker);
                            mapView.invalidate(); // Refresh the map
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } else {
            Log.e("MapFragment", "Map clicked. Plotting attendees for event: " + eventName);
            plotEventAttendees(eventName);
        }
        if (targetLatitude != 0.0 && targetLongitude != 0.0 && initialMarker == null) {
            // Only create the initial marker if it hasn't been created yet
            GeoPoint startPoint = new GeoPoint(targetLatitude, targetLongitude);
            initialMarker = new Marker(mapView);
            initialMarker.setPosition(startPoint);
            initialMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(initialMarker);
            mapController.setCenter(startPoint);
        }
         else {
            Log.e("MapFragment", "Map clicked. Plotting attendees for event: " + eventName);
            plotEventAttendees(eventName);
        }

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        view.findViewById(R.id.button_back_button).setOnClickListener(v -> requireActivity().onBackPressed());
        setupMap();
        view.findViewById(R.id.saveLocationButton).setOnClickListener(v -> {
            if (lastMarker != null) {
                // Log the saved location
                double savedLatitude = lastMarker.getPosition().getLatitude();
                double savedLongitude = lastMarker.getPosition().getLongitude();
                Log.d("MapFragment", "Saved location: Latitude = " + savedLatitude + ", Longitude = " + savedLongitude);

                // Notify the listener
                notifyLocationPicked(savedLatitude, savedLongitude);
                Toast.makeText(getContext(), "Location Saved: " + savedLatitude + ", " + savedLongitude, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "No location selected", Toast.LENGTH_SHORT).show();
            }
        });
        return view;
    }


    private void setupMap() {
        IMapController mapController = mapView.getController();
        mapController.setZoom(15.0);
        mapView.setMultiTouchControls(true);

        // Add tap listener to place a marker
        mapView.getOverlays().add(new Overlay() {
            @Override
            public void draw(Canvas canvas, MapView osmv, boolean shadow) {}

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e, MapView osmv) {
                Projection projection = mapView.getProjection();
                GeoPoint geoPoint = (GeoPoint) projection.fromPixels((int)e.getX(), (int)e.getY());

                // Save coordinates for the dropped pin
                double markerLatitude = geoPoint.getLatitude();
                double markerLongitude = geoPoint.getLongitude();
                placeMarker(geoPoint);

                // Optionally, save these coordinates or pass them to another component
                // For example, save them to a database or pass them back to the previous Fragment

                return true; // Return true to indicate we've handled this event
            }
        });
    }

    private void placeMarker(GeoPoint point) {
        if (lastMarker != null) {
            // Remove only the last user-placed marker, not the initial marker
            mapView.getOverlays().remove(lastMarker);
        }

        // Create and add the new marker
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(point);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(startMarker);
        lastMarker = startMarker; // Update the last marker reference

        // Optionally remove the initial marker since a new place has been selected by the user
        if (initialMarker != null) {
            mapView.getOverlays().remove(initialMarker);
            initialMarker = null; // Ensure the initial marker is no longer referenced
        }

        mapView.invalidate(); // Refresh the map
        notifyLocationPicked(point.getLatitude(), point.getLongitude());
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