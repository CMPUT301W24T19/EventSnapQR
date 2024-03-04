package com.example.eventsnapqr;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
    private List<String> eventNames;
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

    @SuppressLint("MissingInflatedId")
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_your_event, container, false);

        /*
         * Replace this with the database entry
         * */
        attendeeListView = view.findViewById(R.id.attendee_list);
        eventNames = new ArrayList<>();
        eventNames.add("Attendee 1");
        eventNames.add("Attendee 2");
        eventNames.add("Attendee 3");
        eventNames.add("Attendee 4");
        eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, eventNames);

        attendeeListView.setAdapter(eventAdapter);

        view.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        attendeeListView.setOnItemClickListener((parent, view1, position, id) -> {
            String attendeeName = eventNames.get(position);
            CreateDialog(attendeeName);
        });

        view.findViewById(R.id.real_time_attendance_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToRealTimeAttendanceFragment();
            }
        });

        view.findViewById(R.id.attendee_map_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_2);
                Bundle bundle = new Bundle();
                bundle.putString("eventName", eventName);
                navController.navigate(R.id.action_yourEventFragment_to_mapFragment, bundle);
            }
        });

        return view;
    }

    public void ToRealTimeAttendanceFragment(){
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_2);
        navController.navigate(R.id.action_yourEventFragment_to_realTimeAttendanceFragment);
    }

    public void CreateDialog(String attendeeName){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(attendeeName + " has checked in your event Y times.")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // START THE GAME!
                    }
                })
                .setNegativeButton("View on Map", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Replace this with the database entry
                        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_2);
                        Bundle bundle = new Bundle();
                        bundle.putString("eventName", eventName);
                        navController.navigate(R.id.action_yourEventFragment_to_mapFragment, bundle);
                    }
                    });

        builder.create().show();
    }

}
