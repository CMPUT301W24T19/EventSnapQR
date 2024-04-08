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
import org.osmdroid.util.BoundingBox;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.ArrayList;
import java.util.List;

/**
 * fragment where the map is plotted for the event clicked
 * the following sources were a major help  for setting this up
 *
 * I used OpenAI: chatGPT to get the structure of how to plot the coordinates
 * using osmdroid. Prompt "How can I plot a set of coordinated using osmdroid."
 * Prompt "Initialize OSMDroid configuration"
 * also had prompts where I entered errors I received on the way to ask for the
 * reason and possible solution.
 *
 * The startLocationUpdates, onProvider, onLocation change, onResume, onPause
 * permission check and request is taken from OpenAI: chatgpt directly.
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
    public MapFragment() {
        // Required empty public constructor
    }
    public MapFragment(String eventName) {
        this.eventName = eventName;
    }

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().load(getContext(), getActivity().getSharedPreferences("osmdroid", 0));
        db = FirebaseFirestore.getInstance();
        if (getArguments() != null) {
            eventName = getArguments().getString("eventName");
        }
    }

    /**
     * Setup actions to be taken upon view creation and when the views are interacted with
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return the final view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendee_map, container, false);
        TextView text = view.findViewById(R.id.page_name);
        text.setText("Map of " + eventName + " Attendees");
        mapView = view.findViewById(R.id.mapView);
        mapContainer = view.findViewById(R.id.mapContainer);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(16);
        Log.e("MapFragment", "Map clicked. Plotting attendees for event: " + eventName);
        plotEventAttendees(eventName);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

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

    /**
     * This method is pasted from chat gpt as mentioned above
     * it gets the user location and deals with when permissions are
     * given or disabled and manges the status of the user. On location
     * change, the map is also refreshed by the new location.
     *
     * cite: OpenAI: chatgpt for this method, prompt given in the header
     * and the 3 videos mentioned in the header
     */
    private void startLocationUpdates() {
        LocationManager locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);
        LocationListener locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mapView.invalidate();
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
            locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
        } catch (SecurityException e) {
            e.printStackTrace();
            Toast.makeText(requireContext(), "Error getting location: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    /**
     * The method takes in event name as the parameter and goes on firebase
     * and gets the collection/document with that event name, queries the
     * attendee list attending the event and plots them on the map
     *
     * Did use chat gpt to consult the errors I received and asked for solutions
     *
     * @param eventName passing the event name as reference to which
     * event attendee's list to fetch.
     */
    private void plotEventAttendees(String eventName) {
        db.collection("events")
                .whereEqualTo("eventName", eventName)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            String eventId = document.getId();
                            db.collection("events").document(eventId)
                                    .collection("attendees")
                                    .get()
                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                        @Override
                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                            List<GeoPoint> points = new ArrayList<>();
                                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                String latitudeStr = document.getString("latitude");
                                                String longitudeStr = document.getString("longitude");
                                                Log.e("MapFragment", "Found latitude " + latitudeStr);
                                                Long checkINLong = document.getLong("checkedIn");
                                                Log.e("MapFragment", "Found checkIN:  " + checkINLong);

                                                if (checkINLong != null && !latitudeStr.isEmpty() && latitudeStr!=null && !longitudeStr.isEmpty() && longitudeStr!=null && latitudeStr != "0.0" && longitudeStr != "0.0") {
                                                    int checkIN = checkINLong.intValue();
                                                    double latitude = Double.parseDouble(latitudeStr);
                                                    double longitude = Double.parseDouble(longitudeStr);
                                                    Log.e("MapFragment", "Found latitude " + latitude);

                                                    if(checkIN>0) {
                                                        points.add(new GeoPoint(latitude, longitude));
                                                        Marker marker = new Marker(mapView);
                                                        marker.setPosition(new GeoPoint(latitude, longitude));
                                                        String attendeeId = document.getId();
                                                        db.collection("users").document(attendeeId)
                                                                .get()
                                                                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                                                                        if (documentSnapshot.exists()) {
                                                                            String userName = documentSnapshot.getString("name");
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
                                                        marker.setSnippet("Checked In: " + checkIN);
                                                        mapView.getOverlays().add(marker);
                                                    }
                                                }
                                            }
                                            if (!points.isEmpty()) {
                                                if (points.size() == 1) {
                                                    mapView.getController().setCenter(points.get(0));
                                                } else {
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