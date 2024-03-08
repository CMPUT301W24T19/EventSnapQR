package com.example.eventsnapqr;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

/**
 * fragment for admin to browse all profiles currently in the database
 */
public class AdminBrowseProfilesFragment extends Fragment {

    private ListView listView;
    private ProfileAdapter adapter;
    private ArrayList<User> profileList;
    private ImageView buttonBackToAdminMain; // Changed to ImageView
    private ArrayList<User> usersDataList;
    private ProfileAdapter profileAdapter;
    private FirebaseController firebaseController = new FirebaseController();

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
        adapter = new ProfileAdapter(requireContext(),profileList);
    }

    private void showDeleteConfirmationDialog(final User user) {
        new AlertDialog.Builder(getContext())
                .setTitle("Delete User")
                .setMessage("Are you sure you want to delete this user?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // User clicked "Yes", so delete the user
                        firebaseController.deleteUser(user);
                    }
                })
                .setNegativeButton("No", null) // Nothing happens on click.
                .show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_profiles, container, false);

        listView =  view.findViewById(R.id.rv_profile_thumbnails);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User selectedUser = (User) listView.getItemAtPosition(position);
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                Bundle bundle = new Bundle();
                bundle.putString("userId", selectedUser.getDeviceID());
                navController.navigate(R.id.adminUserDetailsFragment,bundle);
                showDeleteConfirmationDialog(selectedUser);
            }
        });

        FirebaseController.OnAllUsersLoadedListener listener = new FirebaseController.OnAllUsersLoadedListener() {
            @Override
            public void onUsersLoaded(List<User> users) {
                usersDataList = new ArrayList<>();
                usersDataList.addAll(users);
                profileAdapter = new ProfileAdapter(getContext(),usersDataList);
                listView.setAdapter(profileAdapter);
            }
        };
        firebaseController.getAllUsers(listener);

        buttonBackToAdminMain = view.findViewById(R.id.button_back_button); // Changed to ImageView
        buttonBackToAdminMain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_adminBrowseProfilesFragment_to_AdminModeMainPageFragment);
            }
        });

        loadProfiles();

        return view;
    }

    private void loadProfiles() {
        adapter.notifyDataSetChanged();
    }
}
