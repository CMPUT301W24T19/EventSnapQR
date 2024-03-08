package com.example.eventsnapqr;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

/**
 * the main page of EventSnapQR. Allows the user to go to scanQR to check-in, go to organize
 * new event to setup an event, go to browse events. in the top right is a link to the users
 * profile, and if the user has admin privileges there is an admin button in the top left to
 * enter admin mode
 */
public class MainPageFragment extends Fragment {
    private Button buttonOrganizeEvent;
    private Button buttonAdminMainPage;
    private Button buttonBrowseEvent;
    private Button buttonScanQR;
    private ImageView buttonViewProfile;
    private String androidId;

    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * authenticate the current user to verify if they have admin privileges. this method
     * determines if the admin button is visible or not
     */
    private void authenticateUser(){
        ContentResolver contentResolver = getContext().getContentResolver();
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        FirebaseController.Authenticator listener = new FirebaseController.Authenticator() {
            @Override
            public void onUserExistenceChecked(boolean exists) {
                // do nothing
            }
            @Override
            public void onAdminExistenceChecked(boolean exists) {
                if(exists && !buttonAdminMainPage.isShown()){
                    buttonAdminMainPage.setVisibility(View.VISIBLE);
                }else{
                    buttonAdminMainPage.setVisibility(View.GONE);
                }
            }
        };
        FirebaseController.checkUserExists(androidId, listener);

    }

    /**
     * handles button presses throughout the fragment
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_page, container, false);
        buttonAdminMainPage = view.findViewById(R.id.admin_button);
        buttonAdminMainPage.setVisibility(View.INVISIBLE);
        authenticateUser();
        buttonOrganizeEvent = view.findViewById(R.id.organize_event_button);
        buttonBrowseEvent = view.findViewById(R.id.browse_events_button);
        buttonScanQR = view.findViewById(R.id.scan_qr_button);
        buttonViewProfile = view.findViewById(R.id.view_user_button);
        updateProfilePicture();
        buttonAdminMainPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_mainPageFragment_to_adminModeMainPageFragment);
            }
        });
        buttonOrganizeEvent.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), OrganizeAnEventActivity.class);
                startActivity(intent);
            }
        });
        buttonBrowseEvent.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment);
                navController.navigate(R.id.action_mainPageFragment_to_browseEventFragment);
            }
        }));
        buttonScanQR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), ScanQRActivity.class);
                intent.putExtra("androidId", androidId);
                startActivity(intent);
            }
        });
        buttonViewProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), UserInfoActivity.class);
                intent.putExtra("androidId", androidId);
                startActivity(intent);
            }
        });
        return view;
    }

    /**
     * populate the imageView in the top right corner
     */
    private void updateProfilePicture() {
        FirebaseController.getInstance().getUser(androidId, new FirebaseController.OnUserRetrievedListener() {
            @Override
            public void onUserRetrieved(User user) {
                if (user != null && user.getProfilePicture() != null && !user.getProfilePicture().isEmpty()) {
                    Glide.with(getContext())
                            .load(user.getProfilePicture())
                            .circleCrop()
                            .into(buttonViewProfile);
                } else {
                    // Optionally, set a default image if there's no profile picture
                    buttonViewProfile.setImageResource(R.drawable.profile_pic); // Adjust with your default drawable
                }
            }
        });
}
}