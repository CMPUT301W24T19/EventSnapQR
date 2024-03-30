package com.example.eventsnapqr;

import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class ViewUserProfileFragment extends Fragment {
    ImageView backToOrganizedEvents;

    public ViewUserProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_view_user_profile, container, false);

        TextView textViewName = view.findViewById(R.id.editTextOrganizerName);
        TextView textViewEmail = view.findViewById(R.id.editTextDescription);
        TextView textViewPhone = view.findViewById(R.id.editTextStartDateTime);
        TextView textViewHomepage = view.findViewById(R.id.editTextEndDateTime);
        ImageView imageViewProfilePic = view.findViewById(R.id.iv_profile_pic);

        // Retrieve the attendeeId passed through the arguments
        Bundle arguments = getArguments();
        if (arguments != null && arguments.containsKey("attendeeId")) {
            String attendeeId = arguments.getString("attendeeId");
            // Fetch the user details from Firestore
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            FirebaseController.getInstance().getUser(attendeeId, new FirebaseController.OnUserRetrievedListener() {
                @Override
                public void onUserRetrieved(User user) {
                    if (user != null) {
                        String name = user.getName();
                        String email = user.getEmail();
                        String phone = user.getPhoneNumber();
                        String homepage = user.getHomepage();
                        String profilePicUri = user.getProfilePicture();

                        if (profilePicUri != null && !profilePicUri.isEmpty()) {
                            Glide.with(getContext())
                                    .load(Uri.parse(profilePicUri))
                                    .into(imageViewProfilePic);
                        } else {
                            Bitmap initialsImageBitmap = user.generateInitialsImage(name);
                            imageViewProfilePic.setImageBitmap(initialsImageBitmap);
                        }

                        textViewName.setText(name);
                        textViewEmail.setText(email);
                        textViewPhone.setText(phone);
                        textViewHomepage.setText(homepage);
                    }
                }
            });

        }
        backToOrganizedEvents = view.findViewById(R.id.back_button);
        backToOrganizedEvents.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });



        return view;
    }

}
