package com.example.eventsnapqr;

import android.content.Intent;
import android.graphics.Bitmap;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import java.io.ByteArrayOutputStream;

/**
 * fragment where a user can organize an event using an eventName, an optional poster (default image
 * otherwise), a description and an optional max attendee. the option for reuse QR code is not yet
 * implemented
 */
public class OrganizeEventFragment extends Fragment {
    private ImageView backButton;
    private Button addEventButton;
    private Button reuseQRButton;
    private EditText editTextEventName;
    private EditText editTextEventDesc;
    private EditText editTextMaxAttendees;
    private ImageView uploadPosterButton;
    private EditText editAnnouncements;
    private String androidID;
    private FirebaseController firebaseController = new FirebaseController();
    private StorageReference storageRef = FirebaseStorage.getInstance().getReference();
    private Uri imageUri;
    private String posterUriString;
    private String qrUriString;

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
        addEventButton = view.findViewById(R.id.button_create);
        reuseQRButton = view.findViewById(R.id.button_reuse_qr);
        editTextEventName = view.findViewById(R.id.editTextEventName);
        editTextEventDesc = view.findViewById(R.id.editTextEventDesc);
        editTextMaxAttendees = view.findViewById(R.id.editTextMaxAttendees);
        uploadPosterButton = view.findViewById(R.id.upload_poster_button);
        editAnnouncements = view.findViewById(R.id.editAnnouncements);

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
        reuseQRButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ;
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
        Integer eventMaxAttendees = !maxAttendeesInput.isEmpty() ? Integer.valueOf(maxAttendeesInput) : null;
        String announcement = editAnnouncements.getText().toString();

        // retrieve user from the database based on the androidID, create a new user and event object
        FirebaseController.getInstance().getUser(androidID, new FirebaseController.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(User user) {
                if (user != null) {
                    String eventID = FirebaseController.getInstance().getUniqueEventID();
                    BarcodeEncoder barcodeEncoder = new BarcodeEncoder();

                    try {
                        Bitmap qrBitmap = barcodeEncoder.encodeBitmap(eventID, BarcodeFormat.QR_CODE, 400, 400);
                        if (qrBitmap != null) {
                            Log.d("QR_CODE", "QR Code generated successfully");
                        } else {
                            Log.e("QR_CODE", "Failed to generate QR Code: Bitmap is null");
                        }
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("bitmap", qrBitmap);

                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        qrBitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                        byte[] qrBytes = outputStream.toByteArray();
                        UploadTask uploadQR = storageRef.child("eventQR/" + eventID).putBytes(qrBytes);
                        while (uploadQR.isInProgress()) {}
                        storageRef.child("eventQR/" + eventID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                qrUriString = uri.toString();
                                if (imageUri != null) {
                                    StorageReference userRef = storageRef.child("eventPosters/" + eventID);  // specifies the path on the cloud storage
                                    UploadTask posterUpload = userRef.putFile(imageUri);
                                    while (posterUpload.isInProgress()) {}
                                    Log.d("TAG", "True1");
                                    userRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            posterUriString = uri.toString();
                                            Log.d("TAG", "QR URI: " + qrUriString);
                                            Event newEvent = new Event(user, eventName, eventDesc, posterUriString, eventMaxAttendees, eventID, announcement, qrUriString);
                                            Log.d("USER NAME", newEvent.getOrganizer().getName());
                                            firebaseController.addEvent(newEvent);
                                            firebaseController.addOrganizedEvent(user, newEvent);
                                            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                                            navController.navigate(R.id.action_organizeEventFragment_to_qRDialogFragment, bundle);
                                        }
                                    });
                                } else {
                                    posterUriString = null;
                                    Event newEvent = new Event(user, eventName, eventDesc, posterUriString, eventMaxAttendees, eventID, announcement, qrUriString);
                                    Log.d("USER NAME", newEvent.getOrganizer().getName());
                                    firebaseController.addEvent(newEvent);
                                    firebaseController.addOrganizedEvent(user, newEvent);
                                    NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                                    navController.navigate(R.id.action_organizeEventFragment_to_qRDialogFragment, bundle);
                                }
                            }
                        });
                    } catch (WriterException e) {
                        e.printStackTrace();
                        Log.e("QR_CODE", "Failed to generate QR Code: " + e.getMessage());
                    }
                }
            }
        });
    }

}
