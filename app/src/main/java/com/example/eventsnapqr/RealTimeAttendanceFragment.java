package com.example.eventsnapqr;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class RealTimeAttendanceFragment extends Fragment {

    private FirebaseController firebaseController;

    private ListView attendeeListView;
    private ArrayAdapter<String> eventAdapter;
    private List<String> eventNames;
    private FirebaseFirestore db;
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_real_time_attendance, container, false);

        /*
         * Replace this with the database entry
         * */
        attendeeListView = view.findViewById(R.id.events);
        eventNames = new ArrayList<>();
        eventNames.add("Attendee 1");
        eventNames.add("Attendee 2");
        eventNames.add("Attendee 3");
        eventNames.add("Attendee 4");
        eventNames.add("Attendee 5");
        eventNames.add("Attendee 6");
        eventNames.add("Attendee 7");
        eventNames.add("Attendee 8");
        eventNames.add("Attendee 9");
        eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, eventNames);

        attendeeListView.setAdapter(eventAdapter);

        view.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        return view;
    }
}
