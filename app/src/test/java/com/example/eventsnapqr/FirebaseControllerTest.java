package com.example.eventsnapqr;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.Test;

import java.util.List;

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
    public void deleteUserTest() {

    }

    @Test
    public void deleteOrganizedEventsTest() {

    }
    @Test
    public void deleteUserFinalStepTest() {}
    @Test
    public void fetchAndDeleteEventTest() {}
    @Test
    public void deleteEventTest() {}
    @Test
    public void removeFromUsersCollectionsTest() {}
    @Test
    public void addUserTest() {}
    @Test
    public void parseDocumentsTest() {}
    @Test
    public void getAllEventsTest() {}
    @Test
    public void getUniqueEventIDTest() {}
    @Test
    public void addEventTest() {}
    @Test
    public void getAllUsersTest() {}
    @Test
    public void parseUsersTest() {}
    @Test
    public void getUserTest() {}
    @Test
    public void getEventTest() {

    }
    @Test
    public void addOrganizedEventTest() {

    }
    @Test
    public void addPromiseToGoTest() {

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