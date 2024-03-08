package com.example.eventsnapqr;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminBrowseImagesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminBrowseImagesFragment extends Fragment {

    private RecyclerView recyclerView;
    private String mParam1;
    private String mParam2;
    FloatingActionButton buttonBackToAdminMain;
    private List<Event> posters;

    public AdminBrowseImagesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminBrowseImagesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminBrowseImagesFragment newInstance(String param1, String param2) {
        AdminBrowseImagesFragment fragment = new AdminBrowseImagesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_images, container, false);
        buttonBackToAdminMain = view.findViewById(R.id.button_back_button);
        recyclerView = view.findViewById(R.id.rv_event_posters);

        posters = new ArrayList<>();
        FirebaseController.getInstance().getAllEvents(new FirebaseController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(ArrayList<Event> events) {
                posters.addAll(events);
            }
        });

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        EventPosterAdapter adapter = new EventPosterAdapter(posters);
        recyclerView.setAdapter(adapter);
        FirebaseFirestore.getInstance().collection("events").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                posters.clear();
                for (QueryDocumentSnapshot doc: value) {
                    String posterUri = (String) doc.getData().get("posterURI");
                    Event event = new Event(null, null, null, null, posterUri, null, null, null);
                    posters.add(event);
                }
                adapter.notifyDataSetChanged();
            }
        });
        buttonBackToAdminMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_adminBrowseImagesFragment_to_AdminModeMainPageFragment);
            }
        });

        adapter.setOnClickListener(new EventPosterAdapter.OnClickListener() {
            @Override
            public void onClick(int position, Event event) {
                Intent intent = new Intent(getContext(), EventPosterActivity.class);
                intent.putExtra("uri", event.getPosterUri());
                startActivity(intent);
            }
        });

        return view;
    }
}