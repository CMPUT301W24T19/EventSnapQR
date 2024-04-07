package com.example.eventsnapqr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageView;

import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Objects;

/**
 * fragment for admin to browse all profiles currently in the database
 */
public class AdminBrowseProfilesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private ArrayList<User> profileList;
    private ImageView buttonBackToAdminMain;
    public AdminBrowseProfilesFragment() {
        // Required empty public constructor
    }

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileList = new ArrayList<>();
        adapter = new ProfileAdapter(profileList);
    }

    /**
     * alert dialog to confirm if the admin wants to delete the given user
     * @param user user object that may be deleted
     */
    private void showDeleteConfirmationDialog(final User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Yes", so delete the user
                        Runnable completionCallback = null;
                        FirebaseController.getInstance().deleteUser(user);
                    }
                })
                .setNegativeButton("No", null) // Nothing happens on click.
                .show();
    }

    private void showUnableToDeleteDialog() {
        new AlertDialog.Builder(getContext())
                .setTitle("Cannot Delete")
                .setMessage("You cannot delete yourself.")
                .setPositiveButton("Okay", null)
                .create()
                .show();
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
        View view = inflater.inflate(R.layout.fragment_admin_browse_profiles, container, false);

        recyclerView = view.findViewById(R.id.user_profile_pictures);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerView.setAdapter(adapter);
        String androidId = Settings.Secure.getString(getContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseFirestore.getInstance().collection("users").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                profileList.clear();
                Log.d("TAG", "Snapshot");
                for (QueryDocumentSnapshot doc: value) {
                    String deviceID = (String) doc.getId();
                    String userName = (String) doc.getData().get("name");
                    String homePage = (String) doc.getData().get("homepage");
                    String phoneNumber = (String) doc.getData().get("phoneNumber");
                    String email = (String) doc.getData().get("email");
                    String profilePicture = (String) doc.getData().get("profileURI");

                    if (homePage == null || homePage.isEmpty()) {
                        homePage = "N/A";
                    }
                    if (phoneNumber == null || phoneNumber.isEmpty()) {
                        phoneNumber = "N/A";
                    }
                    if (email == null || email.isEmpty()) {
                        email = "N/A";
                    }

                    if (userName != null && userName.length() > 16) {
                        userName = userName.substring(0, 14) + "...";
                    }

                    User user = new User(userName, deviceID, homePage, phoneNumber, email);
                    user.setProfilePicture(profilePicture);
                    Log.d("TAG", "Profile: " + userName);
                    profileList.add(user);
                }

                adapter.notifyDataSetChanged();
            }
        });

        adapter.setOnClickListener(new ProfileAdapter.OnClickListener() {
            @Override
            public void onClick(int position, User user) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle("User information")
                        .setMessage("Name: " + user.getName() + "\n"
                                + "Home Page: " + user.getHomepage() + "\n"
                        + "Phone Number: " + user.getPhoneNumber() + "\n"
                        + "Email: " + user.getEmail() + "\n")
                        .setPositiveButton("View", (dialog, which) -> {
                            Intent intent = new Intent(getContext(), UserInfoActivity.class);
                            intent.putExtra("androidId", user.getDeviceID());
                            intent.putExtra("showSwitches", false);
                            startActivity(intent);
                        })
                        .setNegativeButton("Delete", (dialog, which) -> {
                            if (Objects.equals(user.getDeviceID(), androidId)) {
                                showUnableToDeleteDialog();
                            } else {
                                showDeleteConfirmationDialog(user);
                            }
                        })
                        .setNeutralButton("Cancel", null)
                        .create()
                        .show();
            }
        });

        buttonBackToAdminMain = view.findViewById(R.id.button_back_button); // Changed to ImageView
        buttonBackToAdminMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_adminBrowseProfilesFragment_to_AdminModeMainPageFragment);
            }
        });

        return view;
    }
}
