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
    private List<Object> posters;

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
        ImageAdapter adapter = new ImageAdapter(posters); // Change the adapter type
        recyclerView.setAdapter(adapter);

        // fetch both events and users from Firestore and populate the posters list
        FirebaseFirestore.getInstance().collection("events").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                posters.clear();
                for (QueryDocumentSnapshot doc : value) {
                    String eventID = (String) doc.getId();
                    boolean eventActivity = (boolean) doc.getBoolean("active");
                    if (eventActivity) {
                        String eventName = (String) doc.getData().get("eventName");
                        String posterUri = (String) doc.getData().get("posterURI");
                        Event event = null;
                        if (posterUri != null) {
                            event = new Event(null, eventName, null, posterUri, null, eventID, null, null, true, 0, 0);
                        }
                        posters.add(event);
                    }
                }
                adapter.notifyDataSetChanged();
            }
        });

        FirebaseFirestore.getInstance().collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for (QueryDocumentSnapshot doc : value) {
                    String profileUri = doc.getString("profileURI");
                    String userName = doc.getString("name");
                    String deviceId = doc.getString("deviceID");
                    if (profileUri != null) {
                        User user = new User(userName, deviceId);
                        user.setProfilePicture(profileUri);
                        posters.add(user);
                    }
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

        // Set the click listener for both event posters and user profile images
        adapter.setOnClickListener(new ImageAdapter.OnClickListener() {
            @Override
            public void onClick(int position, Object item) {
                if (item instanceof Event) {
                    Event event = (Event) item;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Image Details")
                            .setMessage("Type: " + "Event Poster" + "\n"
                                    + "Event Name: " + event.getEventName() + "\n")
                            .setPositiveButton("View Poster", (dialog, which) -> {
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
                } else if (item instanceof User) {
                    User user = (User) item;
                    AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                    builder.setTitle("Image Details  ")
                            .setMessage("Type: " + "Profile Picture" + "\n"
                                    + "User name: " + user.getName() + "\n")
                            .setPositiveButton("View Poster", (dialog, which) -> {
                                Intent intent = new Intent(getContext(), AdminViewImageActivity.class);
                                intent.putExtra("uri", user.getProfilePicture());
                                startActivity(intent);
                            })
                            .setNegativeButton("Delete", (dialog, which) -> {
                                showDeleteConfirmationDialog(user);
                            })
                            .setNeutralButton("Cancel", null)
                            .create()
                            .show();
                }
            }
        });

        return view;
    }

    /**
     * Show an alert dialog confirming that the user wants to delete an event poster or user profile image
     * @param item the event or user whose poster/profile image is to be deleted
     */
    private void showDeleteConfirmationDialog(Object item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        String itemName;
        String itemType;
        String itemID;

        if (item instanceof Event) {
            Event event = (Event) item;
            itemName = event.getEventName();
            itemType = "event poster";
            itemID = event.getEventID();

        } else if (item instanceof User) {
            User user = (User) item;
            itemName = user.getName();
            itemType = "profile picture";
            itemID = user.getDeviceID();
        } else {
            return;
        }

        builder.setTitle("Confirm Deletion")
                .setMessage("Are you sure you want to delete the " + itemType + " for '" + itemName + "'?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    if (item instanceof Event) {
                        Event event = (Event) item;
                        if (itemID != null) {
                            FirebaseController.getInstance().deleteImage(event.getPosterURI(), event, getContext());
                            posters.remove(event);
                        }
                    } else if (item instanceof User) {
                        User user = (User) item;
                        if (itemID != null) {
                            FirebaseController.getInstance().deleteImage(user.getProfilePicture(), user, getContext());
                            posters.remove(user);
                        }
                    }
                })
                .setNegativeButton("No", null)
                .create()
                .show();
    }
}