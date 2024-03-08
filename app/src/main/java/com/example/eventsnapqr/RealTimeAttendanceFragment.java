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

/**
 * skeleton fragment to view the realtime attendance (checked-in) to an event.
 * none of the functionality is implemented
 */
public class RealTimeAttendanceFragment extends Fragment {
    private ListView attendeeListView;
    private ArrayAdapter<String> eventAdapter;
    private List<String> eventNames;

    // Sample check-in counts, assuming attendees 1 to 5
    private int[] checkInCounts = {1, 2, 3, 4, 5};

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_real_time_attendance, container, false);
        attendeeListView = view.findViewById(R.id.events);
        eventNames = new ArrayList<>();

        // Adding face attendees with check-in counts
        for (int i = 1; i <= 5; i++) {
            eventNames.add("Attendee " + i + " - Checked in: " + checkInCounts[i - 1] + " times");
        }

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
