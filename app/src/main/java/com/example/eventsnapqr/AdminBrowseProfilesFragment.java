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

import android.util.Log;
import android.view.LayoutInflater;
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
 * fragment for admin to browse all profiles currently in the database
 */
public class AdminBrowseProfilesFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private ArrayList<User> profileList;
    private FloatingActionButton buttonBackToAdminMain;

    public AdminBrowseProfilesFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment AdminBrowseProfilesFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static AdminBrowseProfilesFragment newInstance(String param1, String param2) {
        AdminBrowseProfilesFragment fragment = new AdminBrowseProfilesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        profileList = new ArrayList<>();
        adapter = new ProfileAdapter(profileList);
    }



    private void showDeleteConfirmationDialog(final User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseController.deleteUser(user);
                    }
                })
                .setNegativeButton("No", null) // Nothing happens on click.
                .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_profiles, container, false);

        recyclerView = view.findViewById(R.id.user_profile_pictures);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(adapter);
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
                            // Use the position parameter directly
                            Intent intent = new Intent(getContext(), UserInfoActivity.class);
                            intent.putExtra("androidId", user.getDeviceID());
                            startActivity(intent);
                        })
                        .setNegativeButton("Delete", (dialog, which) -> {
                            showDeleteConfirmationDialog(user);
                        })
                        .setNeutralButton("Cancel", null)
                        .create()
                        .show();
            }
        });

        buttonBackToAdminMain = view.findViewById(R.id.button_back_button);
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
