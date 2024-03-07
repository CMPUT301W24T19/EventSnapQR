package com.example.eventsnapqr;

import static org.junit.Assert.assertEquals;

import androidx.annotation.Nullable;

import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.Before;
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
        firebaseInstance.addAttendee(eventIdentifier, attendee);
        FirebaseFirestore instance = FirebaseFirestore.getInstance();
        CollectionReference attendeeReference = instance.collection("events").document(eventIdentifier).collection("attendees");
    }
}