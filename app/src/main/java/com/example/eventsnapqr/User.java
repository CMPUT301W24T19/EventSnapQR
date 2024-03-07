package com.example.eventsnapqr;


import static android.content.ContentValues.TAG;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user object that has the ability of an
 * organizer and attendee
 */
public class User implements Attendee, Organizer{
//public class User implements Attendee, Organizer {
    private String name; // name of the user
    private String homepage; // users website
    private String phoneNumber; // users phone number
    private String email; // users email
    private String deviceID; // the device id associated with the user
    private List<Event> createdEvents = new ArrayList<>(); // Events created by the user as an organizer
    private List<Event> signedUpEvents = new ArrayList<>();
    private String profilePicture;

    /**
     * Constructor for user using their name and a unique device id
     * @param name
     * @param deviceID
     */
    public User(String name, String deviceID) {
        this.name = name;
        this.deviceID = deviceID;
    }
    public User(String deviceID) {
        this.deviceID = deviceID;
    }
    /**
     * Constructor for user using their name, a unique device id, homepage and contact info
     * @param name name of the user
     * @param deviceID unique id of the users device
     * @param homepage the user can add a homepage if wanted
     * @param phoneNumber the users phone number (pt1 of contact info)
     * @param email the users email (pt2 of contact info)
     */
    public User(String name, String deviceID, String homepage, String phoneNumber, String email) {
        this.name = name;
        this.deviceID = deviceID;
        this.homepage = homepage;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    /**
     * getter method for user name
     * @return name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * setter method for name of the user
     * @param name name of the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter method for homepage
     * @return homepage of the user
     */
    public String getHomepage() {
        return homepage;
    }

    /**
     * setter method for homepage
     * @param homepage desired hompage
     */
    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    /**
     * getter method to retrieve users phone number
     * @return phone number of the user
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * setter method if the user needs to update their number
     * @param phoneNumber new phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * getter method to retrieve users email
     * @return users email
     */
    public String getEmail() {
        return email;
    }

    /**
     * setter method if the user needs to update their email
     * @param email new email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * getter method for associated deviceID of the user
     * @return deviceID
     */
    public String getDeviceID() {
        return deviceID;
    }

    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    public Bitmap generateInitialsImage(String name) {
        // Generating default image from user's name initials
        Bitmap bitmap = Bitmap.createBitmap(550, 550, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.GRAY);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(340);
        paint.setTextAlign(Paint.Align.CENTER);
        String[] names = name.split("\\s+");
        if (names.length > 0) {
            String initials;
            if (names.length == 1) {
                // If user only has first name, displaying first 2 letters on profile
                initials = names[0].substring(0, 2).toUpperCase();
            } else {
                // If 2(First,last,middle) or more names gives displaying initials of first and last name on profile pic
                initials = names[0].substring(0, 1).toUpperCase() + names[names.length - 1].substring(0, 1).toUpperCase();
            }
            canvas.drawText(initials, 260, 360, paint);
        }

        return bitmap;
    }

    public String getProfilePicture() {
        return this.profilePicture;
    }

    public User getUser() {
        return this;
    }
    @Override
    public void checkIntoEvent(String qrCode) {
        // Implementation depends on QR code scanning and event check-in logic
    }

    @Override
    public void uploadProfilePicture(String profilePictureUri) {
        setProfilePicture(profilePictureUri);
    }

    @Override
    public void removeProfilePicture() {
        setProfilePicture(null);
    }
    @Override
    public void updateProfileInfo(String name, String homepage, String phoneNumber, String email) {
        setName(name);
        setHomepage(homepage);
        setPhoneNumber(phoneNumber);
        setEmail(email);
    }

    @Override
    public void receivePushNotifications(String message) {
        // Implementation would involve external services like Firebase Cloud Messaging
    }

    @Override
    public Event viewEventDetails(String eventId) {
        // Placeholder: return event details for the given ID
        return null;
    }

    @Override
    public String generateProfilePictureFromName(String name) {
        return "uri_to_generated_profile_picture_based_on_name";
    }

    @Override
    public void signUpForEvent(Event event) {
        if (!signedUpEvents.contains(event)) {
            signedUpEvents.add(event);
        }
    }

    @Override
    public List<Event> browseEvents() {
        // This would typically involve fetching a list of events from a database or backend service.
        // Return a mocked list of events or fetch from your database.
        return new ArrayList<>();
    }

    @Override
    public List<Event> viewSignedUpEvents() {
        return signedUpEvents;
    }
    @Override
    public Event createNewEvent(String eventName, String eventDescription) {
        // Placeholder: create a new event, generate a unique QR code, and add it to createdEvents
        Event newEvent = new Event();
        createdEvents.add(newEvent);
        return newEvent;
    }

    @Override
    public void reuseQRCodeForEvent(Event event, String qrCode) {

    }

    @Override
    public void reuseQRCodeForEvent(Event event, QR qrCode) {
        // Logic to associate an existing QR code with an event
        event.setQR(qrCode);
        // Update the event in the database
    }

    @Override
    public void viewEventAttendees(String eventId) {

    }

    @Override
    public <OnAttendeesRetrievedListener> void viewEventAttendees(String eventId, OnAttendeesRetrievedListener onAttendeesRetrievedListener) {

    }

    public interface AttendeesCallback { // use this interface to get list of users by calling viewEventAttendees("eventId", new AttendeesCallback()
        void onCallback(List<User> userList);
    }


    @Override
    public void viewEventAttendees(String eventId, AttendeesCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).collection("attendees")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> attendees = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        User attendee = documentSnapshot.toObject(User.class);
                        attendees.add(attendee);
                    }
                    callback.onCallback(attendees);
                })
                .addOnFailureListener(e -> Log.e(TAG, "Error getting event attendees", e));
    }

    @Override
    public void sendNotificationsToAttendees(String eventId, String message) {

    }


//    @Override for final part************************
//    public void sendNotificationsToAttendees(String eventId, String message) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        // Fetch attendees for the given event
//        db.collection("events").document(eventId).collection("attendees")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                        String userId = documentSnapshot.getId();
//                        // Assume there's a method to send notification to a user by userId
//                        sendNotificationToUser(userId, message);
//                    }
//                })
//                .addOnFailureListener(e -> Log.e(TAG, "Error getting event attendees", e));
//    }

    @Override
    public void uploadEventPoster(String eventId, String posterUri) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId)
                .update("posterUri", posterUri)
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Poster updated successfully for event: " + eventId))
                .addOnFailureListener(e -> Log.e(TAG, "Error updating poster for event: " + eventId, e));
    }

    @Override
    public void trackRealTimeAttendance(String eventId) {
        // This method's implementation is more about setting up Firestore to listen to changes in real-time
        // Assuming there's a collection 'checkIns' under each event where check-ins are recorded
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("events").document(eventId).collection("checkIns")
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }
                    // Process each document and potentially update UI or internal state
                    for (QueryDocumentSnapshot doc : snapshots) {
                        Log.d(TAG, "Checked in: " + doc.getId());
                        // Update UI or state as necessary
                    }
                });
    }

    @Override
    public void shareQRCodeImage(String qrCode, String platform) {
        // Assuming you have a URI for the QR code image stored in Firestore Storage
        String qrCodeImageUri = "path_to_your_qr_code_image_in_firestore_storage";
        // Logic to share this URI with other platforms or apps goes here.
        // This can involve creating an intent for sharing with other apps.
    }

    @Override
    public void generatePromotionQRCode(String eventId) {

    }

    @Override
    public void viewCheckInLocations(String eventId) {

    }

    @Override
    public int getCheckInCount(String userId, String eventId) {
        return 0;
    }


//    @Override
//    public void generatePromotionQRCode(String eventId) {
//        // Placeholder: generate a unique promotion QR code that links to the event description and poster
//    }

//    @Override final checkpoint*************
//    public void viewCheckInLocations(String eventId) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("events").document(eventId).collection("checkIns")
//                .get()
//                .addOnSuccessListener(queryDocumentSnapshots -> {
//                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
//                        // Assuming each check-in document contains a location field
//                        GeoPoint location = documentSnapshot.getGeoPoint("location");
//                        if (location != null) {
//                            // Logic to display this location on a map
//                            // For example, markLocationOnMap(location.getLatitude(), location.getLongitude());
//                        }
//                    }
//                })
//                .addOnFailureListener(e -> Log.e(TAG, "Error getting check-in locations", e));
//    }


//    @Override final checkpoint *******************
//    public int getCheckInCount(String userId, String eventId) {
//        // Placeholder: return the number of times the specified user has checked into the event
//        return 0; // Example return value
//    }

    @Override
    public List<User> viewSignedUpAttendees(String eventId) {
        // Assuming 'promisedEvents' is a field in the user document that contains event IDs the user has signed up for
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<User> signedUpAttendees = new ArrayList<>();
        db.collection("users")
                .whereArrayContains("promisedEvents", eventId)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User user = document.toObject(User.class);
                            signedUpAttendees.add(user);
                        }
                        // Process signed up attendees as needed
                    } else {
                        Log.d(TAG, "Error getting signed up attendees: ", task.getException());
                    }
                });
        return signedUpAttendees;
    }

    @Override
    public void limitNumberOfAttendees(String eventId, int limit) {

    }

//    @Override
//    public void limitNumberOfAttendees(String eventId, int limit) {
//        FirebaseFirestore db = FirebaseFirestore.getInstance();
//        db.collection("events").document(eventId)
//                .update("attendeeLimit", limit)
//                .addOnSuccessListener(aVoid -> Log.d(TAG, "Attendee limit updated successfully for event: " + eventId))
//                .addOnFailureListener(e -> Log.e(TAG, "Error updating attendee limit for event: " + eventId, e));
//    }
}
