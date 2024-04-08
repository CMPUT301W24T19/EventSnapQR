package com.example.eventsnapqr;


import static android.content.ContentValues.TAG;
import android.util.Log;
import com.google.firebase.firestore.FirebaseFirestore;
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
public class User implements Attendee, Organizer {
    private String name; // name of the user
    private String homepage; // users website
    private String phoneNumber; // users phone number
    private String email; // users email
    private String deviceID; // the device id associated with the user
    private List<Event> createdEvents = new ArrayList<>(); // Events created by the user as an organizer
    private List<Event> signedUpEvents = new ArrayList<>();
    private String profilePicture;
    /**
     * empty constructor for firebase usage
     */
    public User() {
        //empty constructor
    }

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

    /**
     * setter method to change the picture URI of the user
     * @param profilePicture URI of the new image
     */
    public void setProfilePicture(String profilePicture) {
        this.profilePicture = profilePicture;
    }

    /**
     * returns the user profile images' URI
     * @return URI of the image
     */
    public String getProfilePicture() {
        return this.profilePicture;
    }

    /**
     * used to generate a unique profile image based on a users name
     * Cite: OpenAI: chatGPT: prompt "How to convert text to URI"
     * @param name of the user
     * @return bitmap of the image
     */
    public Bitmap generateInitialsImage(String name) {
        // Generating default image from user's name initials
        Bitmap bitmap = Bitmap.createBitmap(550, 550, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.GRAY);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(340);
        paint.setTextAlign(Paint.Align.CENTER);
        if (name != null) {
            String[] names = name.split("\\s+");

            if (names.length > 0) {
                String initials;
                if (names.length == 1) {
                    // If user only has first name, displaying first 2 letters on profile
                    if(names[0].length() == 1){
                        // just use 1 letter if length of name is 1
                        initials = names[0].substring(0, 1).toUpperCase();
                    }else{
                        initials = names[0].substring(0, 2).toUpperCase();
                    }

                } else {
                    // If 2(First,last,middle) or more names gives displaying initials of first and last name on profile pic
                    initials = names[0].substring(0, 1).toUpperCase() + names[names.length - 1].substring(0, 1).toUpperCase();
                }
                canvas.drawText(initials, 260, 360, paint);
            }
        }

        return bitmap;
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
    public void viewEventAttendees(String eventId) {

    }

    @Override
    public <OnAttendeesRetrievedListener> void
    viewEventAttendees(String eventId, OnAttendeesRetrievedListener onAttendeesRetrievedListener) {}

    public interface AttendeesCallback { // use this interface to get list of users by calling viewEventAttendees("eventId", new AttendeesCallback()
        void onCallback(List<User> userList);
        void onAttendeesLoaded(List<String> attendees);

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
    public void sendNotificationsToAttendees(String eventId, String message) {}

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
    public int getCheckInCount(String userId, String eventId) {
        return 0;
    }

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
}
