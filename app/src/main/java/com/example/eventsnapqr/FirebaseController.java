package com.example.eventsnapqr;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseController {
    private static FirebaseController instance;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference eventReference = db.collection("events");
    private CollectionReference userReference = db.collection("users");
    private CollectionReference adminReference = db.collection("admin");
    FirebaseController() {}

    public static synchronized FirebaseController getInstance() {
        if (instance == null) {
            instance = new FirebaseController();
        }
        return instance;
    }
    public static void checkUserExists(String androidId, final Authenticator listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(androidId);
        DocumentReference admin = db.collection("admin").document(androidId);
        admin.get().addOnCompleteListener(task -> {
            if(task.isSuccessful()){
                DocumentSnapshot document = task.getResult();
                if(document.exists()){
                    Log.d("Admin found", "Admin found: " + androidId);
                    listener.onAdminExistenceChecked(true);
                }
                else {
                    Log.d("Admin not found", "Admin not found: " + androidId);
                    listener.onAdminExistenceChecked(false);
                }
            }
            else {
                Log.d("Error", "Error getting document: " + task.getException());
                listener.onAdminExistenceChecked(false); // Assume not found if there's an error
            }
        });
        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("User found", "User found: " + androidId);
                    listener.onUserExistenceChecked(true);
                } else {
                    Log.d("User not found", "User not found: " + androidId);
                    listener.onUserExistenceChecked(false);
                }
            } else {
                Log.d("Error", "Error getting document: " + task.getException());
                listener.onUserExistenceChecked(false); // Assume user doesn't exist if there's an error
            }
        });
    }
    public interface Authenticator {
        void onUserExistenceChecked(boolean exists);
        void onAdminExistenceChecked(boolean exists);
    }

    public void addUser(User user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("name", user.getName());
        if (user.getHomepage() != null) {
            userData.put("homepage", user.getHomepage());
        }
        userData.put("phoneNumber", user.getPhoneNumber());
        userData.put("email", user.getEmail());
        userData.put("deviceID", user.getDeviceID());
        userData.put("profileURI", user.getProfilePicture());
        CollectionReference userReference = db.collection("users");
        userReference
                .document(user.getDeviceID()) // Assuming deviceID is unique for each user
                .set(userData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("Added user success", "User added successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Added user failure", "Failed to add user: " + e);
                    }
                });
    }
    public void deleteEvent(Event event) {
        String eventId = event.getEventID();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Delete the event from the main 'events' collection.
        db.collection("events").document(eventId).delete()
                .addOnSuccessListener(aVoid -> Log.d("Delete Event", "Event successfully deleted: " + eventId))
                .addOnFailureListener(e -> Log.e("Delete Event", "Error deleting event: " + eventId, e));

        // Now, iterate through all users to remove the event from their 'organizedEvents' and 'promisedEvents' subcollections.
        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String userId = documentSnapshot.getId();

                        // Attempt to delete from 'organizedEvents'.
                        db.collection("users").document(userId).collection("organizedEvents").document(eventId).delete();

                        // Attempt to delete from 'promisedEvents'.
                        db.collection("users").document(userId).collection("promisedEvents").document(eventId).delete();

                        Log.d("Delete Event from User", "Attempted to delete event from user: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Delete Event from Users", "Error accessing users for event deletion", e));
    }


    void parseDocuments(List<DocumentSnapshot> documents) {
        for(DocumentSnapshot doc: documents){
            Event event = new Event();
            QR qr = new QR(doc.getString("QRLink"));
            event.setQR(qr);
            event.setOrganizer(new User(doc.getString("organizerID")));
            //doc.get("attendees");
            event.setDescription(doc.getString("description"));
            event.setEventName(doc.getString("eventName"));
            event.setPosterUri(doc.getString("posterURL"));
            event.setAnnouncement(doc.getString("announcement"));
            events.add(event);
            //Event(User organizer, QR qrCode, String eventName, String description, String posterUrl, Integer maxAttendees)
        }
    }
    public interface OnEventsLoadedListener {
        void onEventsLoaded(ArrayList<Event> events);
    }
    ArrayList<Event> events = new ArrayList<>();
    public void getAllEvents(final OnEventsLoadedListener listener) {
        eventReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    events.clear(); // Clear the events list before adding new data
                    parseDocuments(queryDocumentSnapshots.getDocuments());
                    listener.onEventsLoaded(events);
                }

            }
        });
    }

    public String getUniqueEventID() {
        DocumentReference addedDocRef = eventReference.document();
        return addedDocRef.getId();
    }
    public void addEvent(Event event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", event.getEventName());
        eventData.put("QRLink", event.getQrCode().getLink());
        eventData.put("organizerID", event.getOrganizer().getDeviceID());
        eventData.put("description", event.getDescription());
        eventData.put("announcement",event.getAnnouncement());
        if (event.getPosterUri() != null) {
            eventData.put("posterURI", event.getPosterUri());
        }
        if (event.getMaxAttendees() != null) {
            eventData.put("maxAttendees", event.getMaxAttendees());
        }

        // format document id
        //String documentId = event.getEventName() + "-" + event.getOrganizer().getDeviceID();
        if (eventReference != null) {
            eventReference.document(event.getEventID()).set(eventData)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Added event success", "successfully added event: " + event.getEventID());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Added event failure", "failed to add event: " + e.getMessage());
                        }
                    });
        }
    }
    ArrayList<User> users = new ArrayList<>();
    public void getAllUsers(OnAllUsersLoadedListener listener){
        userReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    users.clear();
                    parseUsers(queryDocumentSnapshots.getDocuments());
                    listener.onUsersLoaded(users);
                }

            }
        });

    }

    void parseUsers(List<DocumentSnapshot> documents){
        for(DocumentSnapshot doc: documents){
            String phoneNumber = doc.getString("phoneNumber");
            String name = doc.getString("name");
            String email = doc.getString("email");
            String deviceID = doc.getString("deviceID");
            String link = doc.getString("profileURI");
            User user = new User(name, deviceID, link, phoneNumber,email);
            users.add(user);
        }
    }
    public interface OnAllUsersLoadedListener{
        void onUsersLoaded(List<User> users);
    }

    /**
     * method that creates a user object based on a given androidID and the associated
     * data from the firestore database. very similar to checkUserExists
     * @param androidID
     * @param listener
     */
    public void getUser(String androidID, OnUserRetrievedListener listener) {
        DocumentReference userRef = db.collection("users").document(androidID);

        userRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("User found", "User found: " + androidID);
                    String name = document.getString("name");
                    String deviceID = androidID;
                    String profileURI = document.getString("profileURI");
                    User user = new User(name, deviceID);
                    user.setProfilePicture(profileURI);
                    listener.onUserRetrieved(user);
                } else {
                    Log.d("User not found", "User not found: " + androidID);
                    listener.onUserRetrieved(null);
                }
            } else {
                Log.d("Error", "Error getting document: " + task.getException());
                listener.onUserRetrieved(null);
            }
        });
    }

    public interface OnUserRetrievedListener {
        void onUserRetrieved(User user);
    }

    /**
     * Method that retrieves event details based on the given event identifier.
     * @param eventIdentifier The identifier of the event to retrieve.
     * @param listener        Listener to handle the event retrieval result.
     */
    public void getEvent(String eventIdentifier, OnEventRetrievedListener listener) {
        DocumentReference eventRef = db.collection("events").document(eventIdentifier);

        eventRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Log.d("Event found", "Event found: " + eventIdentifier);
                    String eventName = document.getString("eventName");
                    String organizerID = document.getString("organizerID");
                    String qrLink = document.getString("QRLink");
                    String description = document.getString("description");
                    String posterUri = document.getString("posterURI");
                    String eventId = eventRef.getId();
                    Integer maxAttendees = document.getLong("maxAttendees") != null ? document.getLong("maxAttendees").intValue() : null;
                    String announcement = document.getString("announcement");


                    // retrieve the user who organized the event
                    getUser(organizerID, new OnUserRetrievedListener() {
                        @Override
                        public void onUserRetrieved(User user) {
                            if (user != null) {
                                Event event = new Event(user, new QR(null, qrLink), eventName, description, posterUri, maxAttendees, eventId, announcement);
                                listener.onEventRetrieved(event);
                            } else {
                                Log.d("Error", "Failed to retrieve organizer details for event: " + eventIdentifier);
                                listener.onEventRetrieved(null);
                            }
                        }
                    });
                } else {
                    Log.d("Event not found", "Event not found: " + eventIdentifier);
                    listener.onEventRetrieved(null);
                }
            } else {
                Log.d("Error", "Error getting document: " + task.getException());
                listener.onEventRetrieved(null);
            }
        });
    }

    /**
     * interface to handle event retrieval.
     */
    public interface OnEventRetrievedListener {
        void onEventRetrieved(Event event);
    }

    /**
     * adds an event reference to the specified users organized events subcollection
     * @param user the user to add the event to
     * @param event the event to add to the user
     */
    public void addOrganizedEvent(User user, Event event) {
        DocumentReference userRef = userReference.document(user.getDeviceID());

        userRef.collection("organizedEvents").document(event.getEventID()).set(new HashMap<>())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Added organized event", "Event added to organized events subcollection for user: " + user.getDeviceID());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Failed to add organized event", "Failed to add event to organized events subcollection for user: " + user.getDeviceID());
                    }
                });
    }

    /**
     * adds an event to the specified users promise to go subcollection
     * @param user the user to add the event to
     * @param event the event to add to the user
     */
    public void addPromiseToGo(User user, Event event) {
        DocumentReference userRef = userReference.document(user.getDeviceID());

        userRef.collection("promisedEvents").document(event.getEventID()).set(new HashMap<>())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Added event to users promised events",
                                "Event added to promised events subcollection for user: " + user.getDeviceID());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Failed to add to users promised events",
                                "Failed to add event to users promised events subcollection for user: " + user.getDeviceID());
                    }
                });
    }


    /**
     * add an attendee to the specified events attendee subcollection
     * @param event the event to add the user to
     * @param user the attendee to add to the list
     */
    public void addAttendeeToEvent(Event event, User user) {
        DocumentReference eventRef = eventReference.document(event.getEventID());
        Map<String, Object> attendeeData = new HashMap<>();
        attendeeData.put("checkedIn", 0); // Set checkedIn field to 0
        eventRef.collection("attendees").document(user.getDeviceID())
                .set(attendeeData)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // On success
                        Log.d("Added attendee to event", "Attendee added to event: " + event.getEventID());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // On failure
                        Log.d("Failed to add attendee to event",
                                "Failed to add attendee to event: " + event.getEventID());
                    }
                });
    }
}
