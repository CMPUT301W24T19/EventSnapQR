package com.example.eventsnapqr;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminBrowseProfilesFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminBrowseProfilesFragment extends Fragment {

    // Assuming you have a RecyclerView in your fragment_admin_browse_profiles.xml
    private RecyclerView recyclerView;
    private ProfileAdapter adapter;
    private List<User> profileList;
    private List<User> userList;
    private List<User> dummyUsers;
    FloatingActionButton buttonBackToAdminMain;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_browse_profiles, container, false);

        dummyUsers = new ArrayList<>();
        for (int i = 1; i <= 30; i++) {
            User user = new User("User " + i, "u");
            user.setHomepage("https://homepage.com/user" + i);
            user.setContactInfo("user" + i + "@example.com");
            dummyUsers.add(user);
        }

        adapter = new ProfileAdapter(dummyUsers);
        recyclerView = view.findViewById(R.id.rv_profile_thumbnails);
        int numberOfColumns = 3;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), numberOfColumns));
        recyclerView.setAdapter(adapter);

        buttonBackToAdminMain = view.findViewById(R.id.button_back_button);
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