package com.example.eventsnapqr;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class OrganizeEventFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private ImageView backButton;
    private Button addEventButton;
    private EditText editTextEventName;
    private EditText editTextEventDesc;

    private Bitmap qrBitmap;

    private String param1;
    private String param2;

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

        backButton = view.findViewById(R.id.button_back_button);
        addEventButton = view.findViewById(R.id.button_create);
        editTextEventName = view.findViewById(R.id.editTextEventName);
        editTextEventDesc = view.findViewById(R.id.editTextEventDesc);

        backButton.setOnClickListener(v -> navigateToMainPageFragment());
        addEventButton.setOnClickListener(v -> {
            if (validateInput()) {
                createEvent();
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

    private void navigateToMainPageFragment() {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
        navController.navigate(R.id.action_organizeEventFragment_to_mainPageFragment);
    }

    private void createEvent() {
        String eventName = editTextEventName.getText().toString();

        QRGEncoder qrgEncoder = new QRGEncoder(generateLink(eventName,"userId"), null, QRGContents.Type.TEXT, 5);
        qrgEncoder.setColorBlack(Color.RED);
        qrgEncoder.setColorWhite(Color.BLUE);
        try {
            qrBitmap = qrgEncoder.getBitmap();
            Bundle bundle = new Bundle();
            bundle.putParcelable("bitmap", qrBitmap);
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
            navController.navigate(R.id.action_organizeEventFragment_to_qRDialogFragment, bundle);
            QR qrCode = new QR(qrBitmap);
            Organizer organizer = new Organizer(new User("username"));
            Event newEvent = new Event(organizer, qrCode, "EventName");
            // getUser().addEvent(newEvent);
            Toast.makeText(requireContext(), "Successfully added event", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.v("ORGANIZE EVENT ERROR", e.toString());
        }
    }
    public String generateLink(String eventName, String eventId){
        String h = "com.example.eventsnapqr://com.example.eventsnapqr/join/event";
        return h+"/"+eventName+"/"+eventId;
    }

}
