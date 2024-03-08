package com.example.eventsnapqr;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Page for admin to edit and delete a users profiles
 */
public class AdminUserDetailsFragment extends Fragment {
    private String userId;
    private FirebaseController firebaseController;
    private User userToShow;

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
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
