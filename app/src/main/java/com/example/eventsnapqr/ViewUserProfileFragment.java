package com.example.eventsnapqr;

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
            DocumentReference userRef = db.collection("users").document(attendeeId);

            userRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    String name = documentSnapshot.getString("name");
                    String email = documentSnapshot.getString("email");
                    String phone = documentSnapshot.getString("phoneNumber");
                    String homepage = documentSnapshot.getString("homepage");
                    String profilePicUri = documentSnapshot.getString("profilePicture");

                    textViewName.setText(name);
                    textViewEmail.setText(email);
                    textViewPhone.setText(phone);
                    textViewHomepage.setText(homepage);

                }
            }).addOnFailureListener(e -> {
                Log.e("ViewUserProfileFragment", "Error fetching user details", e);
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
