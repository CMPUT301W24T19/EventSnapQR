package com.example.eventsnapqr;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class YourEventFragment extends Fragment {

    private FirebaseController firebaseController;

    private ListView attendeeListView;
    private ArrayAdapter<String> eventAdapter;
    private List<String> attendeeNames;
    private FirebaseFirestore db;

    private String eventName;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            eventName = getArguments().getString("eventName");
        }
        firebaseController = new FirebaseController();
    }

    public YourEventFragment() {
        // Required empty public constructor
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_event, container, false);

        /*
         * Replace this with the database entry
         * */
        attendeeListView = view.findViewById(R.id.attendee_list);
        attendeeNames = new ArrayList<>();
        attendeeNames.add("Attendee 1");
        attendeeNames.add("Attendee 2");
        attendeeNames.add("Attendee 3");
        attendeeNames.add("Attendee 4");
        eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, attendeeNames);

        attendeeListView.setAdapter(eventAdapter);

        view.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        attendeeListView.setOnItemClickListener((parent, view1, position, id) -> {
            String attendeeName = attendeeNames.get(position);
            CreateDialog(attendeeName);
        });

        view.findViewById(R.id.real_time_attendance_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_YourEventFragment_to_RealTimeAttendanceFragment);
            }
        });

        view.findViewById(R.id.attendee_map_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_YourEventFragment_to_MapFragment);
            }
        });

        view.findViewById(R.id.notify_attendee_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Context is required for AlertDialog, use getContext() in a fragment.
                // For an activity, you can use 'MyEventActivity.this' directly.
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

                // Set up the input
                final EditText input = new EditText(getContext());
                // Specify the type of input expected
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                builder.setView(input);

                // Set up the buttons
                builder.setPositiveButton("Send", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String notificationText = input.getText().toString();
                        // Handle "Yes" button click
                        // Use 'notificationText' as the text for the notification
                    }
                });
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel(); // Close the dialog
                    }
                });

                // Set the title and message for the dialog
                builder.setTitle("Notification");
                builder.setMessage("Enter notification message:");

                builder.show(); // Show the AlertDialog
            }
        });

        return view;
    }


    public long TimesStatistics(String attendeeName){
        long times = 0;
        for (String attendee : attendeeNames){
            if (attendee.equals(attendeeName)){
                // Replace this with the database entry
                times += 1;
            }
        }
        return times;
    }

    public void CreateDialog(String attendeeName){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        long times = TimesStatistics(attendeeName);
        builder.setMessage(attendeeName + " has checked in your event " + times + " times.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // START THE GAME!
                    }
                })
                .setNegativeButton("View on Map", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Replace this with the database entry
                        //NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_2);
                        //Bundle bundle = new Bundle();
                        //bundle.putString("eventName", eventName);
                        //navController.navigate(R.id.action_yourEventFragment_to_mapFragment, bundle);
                    }
                    });

        builder.create().show();
    }

}
