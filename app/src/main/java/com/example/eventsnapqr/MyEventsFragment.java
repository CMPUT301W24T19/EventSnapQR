package com.example.eventsnapqr;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.firestore.DocumentSnapshot;
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
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_my_events, container, false);
        attend_eventListView = v.findViewById(R.id.attending_events_list);
        orgnize_eventListView = v.findViewById(R.id.organized_events_list);

        eventNames = new ArrayList<>();
        eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, eventNames);
        attend_eventListView.setAdapter(eventAdapter);
        orgnize_eventListView.setAdapter(eventAdapter);

        db = FirebaseFirestore.getInstance();
        loadEvents();

        v.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
            }
        });

        orgnize_eventListView.setOnItemClickListener((parent, view, position, id) -> {
            String eventName = eventNames.get(position);
            goToYourEventActivity(eventName);
        });

        return v;
    }

    public void goToYourEventActivity(String eventName) {
        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_2);
        Bundle bundle = new Bundle();
        bundle.putString("eventName", eventName);
        navController.navigate(R.id.yourEventFragment, bundle);
    }

    private void loadEvents() {
        db.collection("events").get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventNames.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String eventName = document.getId();
                            eventNames.add(eventName);
                        }
                        eventAdapter.notifyDataSetChanged();
                    }
                    else {} // error handling?
                });
    }
}