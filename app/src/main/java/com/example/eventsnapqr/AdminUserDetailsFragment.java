package com.example.eventsnapqr;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminUserDetailsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminUserDetailsFragment extends Fragment {

    private static final String ARG_USER = "user";

    private String userId;

    public AdminUserDetailsFragment() {
        // Required empty public constructor
    }

    public static AdminUserDetailsFragment newInstance(User user) {
        AdminUserDetailsFragment fragment = new AdminUserDetailsFragment();
        Bundle args = new Bundle();

        fragment.setArguments(args);
        return fragment;
    }
    private FirebaseController firebaseController;
    private User userToShow;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userId = getArguments().getString("userId");
            firebaseController = new FirebaseController();
            firebaseController.getUser(userId, new FirebaseController.OnUserRetrievedListener() {
                @Override
                public void onUserRetrieved(User user) {
                    userToShow = user;
                }
            });
        }

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_user_details, container, false);
        final TextView textViewUserName = view.findViewById(R.id.text_view_name);
        final TextView textViewUserID = view.findViewById(R.id.text_view_id);
        TextView textViewHomepage = view.findViewById(R.id.text_view_homepage);
        if (getArguments() != null) {
            String userId = getArguments().getString("userId");
            firebaseController = new FirebaseController();
            firebaseController.getUser(userId, new FirebaseController.OnUserRetrievedListener() {
                @Override
                public void onUserRetrieved(User user) {
                    userToShow = user;
                    if (userToShow != null) {
                        textViewUserName.setText(userToShow.getName());
                        textViewUserID.setText(userToShow.getDeviceID());
                    } else {
                        // Handle the case where userToShow is null
                    }
                }
            });
        }
        return view;
    }

}
