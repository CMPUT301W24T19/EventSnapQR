package com.example.eventsnapqr;

import static androidx.fragment.app.FragmentManager.TAG;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class MyEventsFragment extends Fragment {

    private ListView attend_eventListView, orgnize_eventListView;
    private ArrayAdapter<String> attend_eventAdapter, orgnize_eventAdapter;
    private String androidId;
    private List<String> attend_eventNames, orgnize_eventNames;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_my_events, container, false);
        attend_eventListView = v.findViewById(R.id.attending_events_list);
        orgnize_eventListView = v.findViewById(R.id.orgnized_events_list);
        androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        attend_eventNames = new ArrayList<>();
        orgnize_eventNames = new ArrayList<>();
        attend_eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, attend_eventNames);
        orgnize_eventAdapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_1, orgnize_eventNames);
        attend_eventListView.setAdapter(attend_eventAdapter);
        orgnize_eventListView.setAdapter(orgnize_eventAdapter);

        db = FirebaseFirestore.getInstance();

        v.findViewById(R.id.button_back_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().finish();
            }
        });

        fetchEvents("organized events", orgnize_eventListView, orgnize_eventNames, orgnize_eventAdapter);
        fetchEvents("promisedEvents", attend_eventListView, attend_eventNames, attend_eventAdapter);

        return v;
    }

    private void fetchEvents(String subcollection, ListView listView, List<String> eventNames, ArrayAdapter<String> eventAdapter) {
        db.collection("users")
                .document(androidId)
                .collection(subcollection)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventNames.clear();
                        for (DocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId();
                            db.collection("events")
                                    .document(eventId)
                                    .get()
                                    .addOnSuccessListener(eventDocument -> {
                                        String eventName = eventDocument.getString("event name");
                                        eventNames.add(eventName);
                                        eventAdapter.notifyDataSetChanged();
                                    })
                                    .addOnFailureListener(e -> {
                                    });
                        }
                    } else {
                        Log.d("FETCH ERROR", "Error fetching subcollection documents", task.getException());
                    }
                });
    }

}