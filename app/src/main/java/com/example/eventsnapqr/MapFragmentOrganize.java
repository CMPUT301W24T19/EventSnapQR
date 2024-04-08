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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ZoomControls;

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

    private double targetLatitude = 0.0;
    private double targetLongitude = 0.0;
    private Marker lastMarker = null;
    private Marker initialMarker = null;

    EditText addressTextBox;


    public MapFragmentOrganize() {
    }
    public MapFragmentOrganize(String eventName) {
        this.eventName = eventName;
    }
    public interface OnLocationPickedListener {
        void onLocationPicked(double latitude, double longitude);
    }
    public void setOnLocationPickedListener(OnLocationPickedListener listener) {
        this.listener = listener;
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

            targetLatitude = getArguments().getDouble("latitude", 0.0);
            targetLongitude = getArguments().getDouble("longitude", 0.0);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);

        mapView = view.findViewById(R.id.mapView);
        addressTextBox = view.findViewById(R.id.search_bar);
        mapContainer = view.findViewById(R.id.mapContainer);
        mapView.setTileSource(TileSourceFactory.MAPNIK);
        mapView.setMultiTouchControls(true);
        IMapController mapController = mapView.getController();
        mapController.setZoom(16);

        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

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

                        getActivity().runOnUiThread(() -> {
                            GeoPoint startPoint = new GeoPoint(latitude, longitude);

                            new ReverseGeocodingTask(addressTextBox).execute(startPoint);

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
            Log.e("MapFragmentOrganizer", "Cannot get user location: " + eventName);
        }
        if (targetLatitude != 0.0 && targetLongitude != 0.0 && initialMarker == null) {
            GeoPoint startPoint = new GeoPoint(targetLatitude, targetLongitude);
            initialMarker = new Marker(mapView);
            initialMarker.setPosition(startPoint);
            initialMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(initialMarker);
            mapController.setCenter(startPoint);
        }
         else {
            Log.e("MapFragmentOrganizer", "Did not get user location/invalid user location" + eventName);
        }

        view.findViewById(R.id.button_back_button).setOnClickListener(v -> requireActivity().onBackPressed());
        setupMap();

        view.findViewById(R.id.search_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String searchString = addressTextBox.getText().toString();
                if (!searchString.isEmpty()) {
                    new Thread(() -> {
                        try {
                            Geocoder geocoder = new Geocoder(getContext());
                            List<Address> addresses = geocoder.getFromLocationName(searchString, 1);
                            if (!addresses.isEmpty()) {
                                if (lastMarker != null) {
                                    mapView.getOverlays().remove(lastMarker);
                                }
                                Address address = addresses.get(0);
                                double latitude = address.getLatitude();
                                double longitude = address.getLongitude();

                                getActivity().runOnUiThread(() -> {
                                    GeoPoint searchPoint = new GeoPoint(latitude, longitude);
                                    mapController.setCenter(searchPoint);
                                    Marker searchMarker = new Marker(mapView);
                                    searchMarker.setPosition(searchPoint);
                                    searchMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                                    mapView.getOverlays().add(searchMarker);
                                    lastMarker = searchMarker;
                                    mapView.invalidate();
                                    placeMarker(searchPoint);
                                });
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }).start();
                }
            }
        });



        view.findViewById(R.id.saveButton).setOnClickListener(v -> {
            if (lastMarker != null) {
                double savedLatitude = lastMarker.getPosition().getLatitude();
                double savedLongitude = lastMarker.getPosition().getLongitude();
                Log.d("MapFragment", "Saved location: Latitude = " + savedLatitude + ", Longitude = " + savedLongitude);

                if (listener != null) {
                    listener.onLocationPicked(savedLatitude, savedLongitude);
                }
                if (getActivity() != null) {
                    getActivity().getSupportFragmentManager().popBackStack();
                }
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

        mapView.getOverlays().add(new Overlay() {
            @Override
            public void draw(Canvas canvas, MapView osmv, boolean shadow) {}

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e, MapView osmv) {
                Projection projection = mapView.getProjection();
                GeoPoint geoPoint = (GeoPoint) projection.fromPixels((int)e.getX(), (int)e.getY());

                double markerLatitude = geoPoint.getLatitude();
                double markerLongitude = geoPoint.getLongitude();
                performReverseGeocoding(geoPoint);
                placeMarker(geoPoint);


                return true;
            }
        });
    }
    private void performReverseGeocoding(GeoPoint geoPoint) {
        ReverseGeocodingTask reverseGeocodingTask = new ReverseGeocodingTask(addressTextBox);


        reverseGeocodingTask.execute(geoPoint);
    }

    private void placeMarker(GeoPoint point) {
        if (lastMarker != null) {
            mapView.getOverlays().remove(lastMarker);
        }
        double markerLatitude = point.getLatitude();
        double markerLongitude = point.getLongitude();

        String formattedLatitude = String.format("%.2f", markerLatitude);
        String formattedLongitude = String.format("%.2f", markerLongitude);

        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(point);
        startMarker.setTitle("Latitude: " + formattedLatitude + ", Longitude: " + formattedLongitude);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().add(startMarker);
        lastMarker = startMarker;

        if (initialMarker != null) {
            mapView.getOverlays().remove(initialMarker);
            initialMarker = null;
        }

        mapView.invalidate();
        notifyLocationPicked(point.getLatitude(), point.getLongitude());
    }


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