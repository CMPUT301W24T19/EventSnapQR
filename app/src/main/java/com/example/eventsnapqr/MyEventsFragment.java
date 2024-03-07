package com.example.eventsnapqr;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyEventsFragment extends Fragment {

    public MyEventsFragment() {
        // Required empty public constructor
    }

    private ListView attend_eventListView, orgnize_eventListView;
    private ArrayAdapter<String> eventAdapter;
    private List<String> eventNames;
    private FirebaseFirestore db;
    @SuppressLint("MissingInflatedId")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_my_events, container, false);
        //attend_eventListView = v.findViewById(R.id.attending_events_list);
        //orgnize_eventListView = v.findViewById(R.id.orgnized_events_list);

        /*
        * Should be replaced by entries from the database
        * */
        eventNames = new ArrayList<>();
        eventNames.add("Event 1");
        eventNames.add("Event 2");
        eventNames.add("Event 3");
        eventNames.add("Event 4");
        /*eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, eventNames);
        attend_eventListView.setAdapter(eventAdapter);
        orgnize_eventListView.setAdapter(eventAdapter);*/

        db = FirebaseFirestore.getInstance();

        v.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
            }
        });
/*
        orgnize_eventListView.setOnItemClickListener((parent, view, position, id) -> {
            String eventName = eventNames.get(position);
            goToYourEventActivity(eventName);
        });

        attend_eventListView.setOnItemClickListener((parent, view1, position, id) -> {
            String eventName = eventNames.get(position);
            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_2);
            Bundle bundle = new Bundle();
            bundle.putString("eventName", eventName);
            navController.navigate(R.id.action_myEventsFragment_to_eventDetailFragmentFromGraph2, bundle);
        });
*/
        return v;
    }

    public void goToYourEventActivity(String eventName) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_2);
        Bundle bundle = new Bundle();
        bundle.putString("eventName", eventName);
        navController.navigate(R.id.yourEventFragment, bundle);
    }
}