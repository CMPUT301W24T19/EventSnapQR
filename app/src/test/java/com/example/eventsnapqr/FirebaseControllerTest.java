package com.example.eventsnapqr;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.*;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class FirebaseControllerTest {
   // private FirebaseController mockInstance() {
   //     return FirebaseController.getInstance();
    //}
    //private FirebaseFirestore firestoreInstance() {
     //   return FirebaseFirestore.getInstance();
    //}

    @Test
    public void checkUserExistsTest() {
        String oldUserID = "oldUserID";
        String newUserID = "newUserID";
        String noAdminID = "noAdminID";
        String adminID = "adminID";
        //FirebaseFirestore firebaseFirestore = new FirebaseFirestore.getInstance();
        FirebaseController.Authenticator listener = new FirebaseController.Authenticator() {
            @Override
            public void onUserExistenceChecked(boolean exists) {
                if (exists) {
                }
            }
            @Override
            public void onAdminExistenceChecked(boolean exists) {

            }
        };
    }
/*
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
    public void getAllEventsTest() {}*/
    @Test
    public void getUniqueEventIDTest() {
        /*FirebaseController firebaseController = new FirebaseController();
        ArrayList<String> eventIDs = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            eventIDs.add(firebaseController.getUniqueEventID());
        }
        String extraID = firebaseController.getUniqueEventID();
        assertFalse(eventIDs.contains(extraID));*/
    }
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
        assertTrue(true);
    }
    @Test
    public void addAttendeeTest() {
        String eventIdentifier = "Test Event";
        String userName = "Test User";
        String userID = "Test ID";

        User attendee = new User(userName, userID, "Test Homepage", "Test Phone number", "Test Email");
       // firebaseInstance.addAttendeeToEvent(eventIdentifier, attendee);
        //FirebaseFirestore instance = FirebaseFirestore.getInstance();
        //CollectionReference attendeeReference = instance.collection("events").document(eventIdentifier).collection("attendees");
    }
}