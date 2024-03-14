package com.example.eventsnapqr;

import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.List;

/**
 * Fragment for an admin to browse and delete all images in the database
 */
public class AdminBrowseImagesFragment extends Fragment {
    private RecyclerView recyclerView;
    private ImageView buttonBackToAdminMain;
    private List<Event> posters;

    /**
     * What should be executed when the fragment is created
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Setup actions to be taken upon view creation and when the views are interacted with
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return the final view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_images, container, false);
        buttonBackToAdminMain = view.findViewById(R.id.button_back_button);
        recyclerView = view.findViewById(R.id.rv_event_posters);

        posters = new ArrayList<>();
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        EventPosterAdapter adapter = new EventPosterAdapter(posters);
        recyclerView.setAdapter(adapter);
        FirebaseFirestore.getInstance().collection("events").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                //Log.d("TAG", "New snapshot");
                posters.clear();
                Log.d("TAG", "Snapshot");
                for (QueryDocumentSnapshot doc : value) {
                    String eventID = (String) doc.getId();
                    Log.d("TAG", "Document ID: " + eventID);
                    String eventName = (String) doc.getData().get("eventName");
                    String posterUri = (String) doc.getData().get("posterURI");
                    if (posterUri == null) {continue;}
                    Event event = new Event(null, eventName, null, posterUri, null, eventID, null, null);
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
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("Event Poster for " + event.getEventName())
                        .setPositiveButton("View", (dialog, which) -> {
                            Intent intent = new Intent(getContext(), AdminViewImageActivity.class);
                            intent.putExtra("uri", event.getPosterURI());
                            startActivity(intent);
                        })
                        .setNegativeButton("Delete", (dialog, which) -> {
                            showDeleteConfirmationDialog(event);
                        })
                        .setNeutralButton("Cancel", null)
                        .create()
                        .show();
            }
        });

        return view;
    }

    /**
     * Show an alert dialog confirming that the user wants to delete an event
     *
     * @param event the event to be deleted
     */
    private void showDeleteConfirmationDialog(Event event) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete the poster for '" + event.getEventName() + "'?")
                .setPositiveButton("Yes", (dialog, which) -> { // if yes
                    if (event.getPosterURI() != null) {
                        FirebaseController.getInstance().deleteImage(event.getPosterURI());
                        FirebaseController.getInstance().getEvent(event.getEventID(), new FirebaseController.OnEventRetrievedListener() {
                            @Override
                            public void onEventRetrieved(Event event) {
                                event.setPosterURI(null);
                                FirebaseController.getInstance().addEvent(event);
                            }
                        });
                    } else {
                        Toast.makeText(requireContext(), "Event does not have a poster", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }
}