package com.example.eventsnapqr;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

public class FirebaseControllerTest {
    private FirebaseController firebaseInstance = FirebaseController.getInstance();

    @Test
    public void checkUserExistsTest() {
        FirebaseController.Authenticator listener = new FirebaseController.Authenticator() {
            @Override
            public void onUserExistenceChecked(boolean exists) {

            }

            @Override
            public void onAdminExistenceChecked(boolean exists) {

            }
        };
    }

    @Test
    public void addAttendeeTest() {
        String eventIdentifier = "Test Event";
        String userName = "Test User";
        String userID = "Test ID";

        User attendee = new User(userName, userID, "Test Homepage", "Test Phone number", "Test Email");
       // firebaseInstance.addAttendeeToEvent(eventIdentifier, attendee);
        FirebaseFirestore instance = FirebaseFirestore.getInstance();
        CollectionReference attendeeReference = instance.collection("events").document(eventIdentifier).collection("attendees");
    }
}