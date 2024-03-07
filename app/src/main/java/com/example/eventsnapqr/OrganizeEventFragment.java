package com.example.eventsnapqr;

import static androidx.camera.core.impl.utils.ContextUtil.getBaseContext;

import static com.google.common.hash.Hashing.sha256;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URI;

import java.util.ArrayList;
import java.util.List;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class OrganizeEventFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ImageView backButton;
    private Button addEventButton;
    private EditText editTextEventName;
    private EditText editTextEventDesc;
    private EditText editTextMaxAttendees;
    private Bitmap qrBitmap;
    private ImageView uploadPosterButton;

    private String param1;
    private String param2;
    private String androidID;
    private FirebaseController firebaseController = new FirebaseController();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Uri imageUri;
    private String uriString;

    public OrganizeEventFragment() {
        // Required empty public constructor
    }


    public static OrganizeEventFragment newInstance(String param1, String param2) {
        OrganizeEventFragment fragment = new OrganizeEventFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organize_event, container, false);
        androidID = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d("Device ID", "Android ID: " + androidID);
        backButton = view.findViewById(R.id.button_back_button);
        addEventButton = view.findViewById(R.id.button_create);
        editTextEventName = view.findViewById(R.id.editTextEventName);
        editTextEventDesc = view.findViewById(R.id.editTextEventDesc);
        editTextMaxAttendees = view.findViewById(R.id.editTextMaxAttendees);
        uploadPosterButton = view.findViewById(R.id.upload_poster_button);

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
        addEventButton.setOnClickListener(v -> {
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

    private boolean validateInput() {
        String eventName = editTextEventName.getText().toString().trim();
        String eventDesc = editTextEventDesc.getText().toString().trim();

        if (eventName.isEmpty()) {
            editTextEventName.setError("Event name cannot be empty");
            return false;
        }
/*
        if (eventName.length() > 50) {
            editTextEventName.setError("Event name cannot exceed 50 characters");
            return false;
        }
*/
        if (eventDesc.isEmpty()) {
            editTextEventDesc.setError("Event description cannot be empty");
            return false;
        }
        return true;
    }

    private void navigateToMainPageFragment() {
        Intent intent = new Intent(requireContext(), MainActivity.class);
        startActivity(intent);
    }

    private ArrayList<Event> allEvents;
    private void createEvent() {
        String eventName = editTextEventName.getText().toString(); // get the name of the event
        String eventDesc = editTextEventDesc.getText().toString(); // get the description of the event
        String maxAttendeesInput = editTextMaxAttendees.getText().toString();
        Integer eventMaxAttendees = !maxAttendeesInput.isEmpty() ? Integer.valueOf(maxAttendeesInput) : null;


        // retrieve user from the database based on the androidID, create a new user and event object
        FirebaseController.getInstance().getUser(androidID, new FirebaseController.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(User user) {

                if (user != null) {
                    // User retrieved successfully, proceed with event creation
                    String eventID = FirebaseController.getInstance().getUniqueEventID();
                    String link = generateLink(eventID, user.getDeviceID());
                    Log.d("QR link generated", "QR link: " + link);
                    QRGEncoder qrgEncoder = new QRGEncoder(link, null, QRGContents.Type.TEXT, 5);
                    qrgEncoder.setColorBlack(Color.RED);
                    qrgEncoder.setColorWhite(Color.BLUE);
                    Log.d("TAG", "1");
                    try {
                        qrBitmap = qrgEncoder.getBitmap();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("bitmap", qrBitmap);
                        QR qrCode = new QR(qrBitmap, link);
                        if (imageUri != null) {
                            StorageReference userRef = storageRef.child("eventPosters/" + eventID);  // specifies the path on the cloud storage
                            userRef.putFile(imageUri).addOnSuccessListener(taskSnapshot -> {
                                userRef.getDownloadUrl().addOnSuccessListener(uri -> {
                                    imageUri = uri;
                                    uriString = imageUri.toString();
                                    Log.d("TAG", "Uri string is true");
                                    // Use the retrieved user to create the event
                                    Event newEvent = new Event(user, qrCode, eventName, eventDesc, uriString, eventMaxAttendees, eventID);
                                    Log.d("USER NAME", newEvent.getOrganizer().getName());
                                    firebaseController.addEvent(newEvent);
                                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                                    navController.navigate(R.id.action_organizeEventFragment_to_qRDialogFragment, bundle);
                                });
                            });  // puts the file into the referenced path
                        }
                        else {
                            uriString = null;
                            // Use the retrieved user to create the event
                            Event newEvent = new Event(user, qrCode, eventName, eventDesc, uriString, eventMaxAttendees, eventID);
                            Log.d("USER NAME", newEvent.getOrganizer().getName());
                            firebaseController.addEvent(newEvent);
                            firebaseController.addOrganizedEvent(user, newEvent);
                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                            navController.navigate(R.id.action_organizeEventFragment_to_qRDialogFragment, bundle);
                        }
                        /*if(newEvent != null){
                            firebaseController = FirebaseController.getInstance();
                            //ArrayList<Event> allEvents;
                            firebaseController.getEvents(new FirebaseController.OnEventsLoadedListener(){
                                @Override
                                public void onEventsLoaded(ArrayList<Event> events) {
                                    allEvents = new ArrayList<>();
                                    allEvents.addAll(events);
                                    if (!checkEventExists(newEvent, allEvents)) {
                                        firebaseController.addEvent(newEvent);
                                        firebaseController.addOrganizedEvent(user, newEvent);
                                        Log.d("TAG", "Event ID " + newEvent.getEventID());
                                        Toast.makeText(requireContext(), "Successfully added event", Toast.LENGTH_LONG).show();
                                    }
                                    else{
                                        Toast.makeText(requireContext(), "Error: Event already exists", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });


                        }*/

                    } catch (Exception e) {
                        Log.v("ORGANIZE EVENT ERROR", e.toString());
                    }
                } else {
                    // Handle case where user is not found
                    Log.d("User not found", "User with ID " + androidID + " not found");
                }
            }
        });
    }
    boolean checkEventExists(Event event, ArrayList<Event> events){
        for(Event e : events){
            if(e.getQrCode().getLink().equals(event.getQrCode().getLink())){
                return true;
            }
        }
        return false;
    }
    public String generateLink(String eventName, String organizerId) {
        // eventsnapqr://com.example.eventsnapqr/join/event <-- link prefix
        String prefix = "eventsnapqr://com.example.eventsnapqr/join/event";
        return prefix + "/" + organizerId + "/" + eventName;
    }

}
