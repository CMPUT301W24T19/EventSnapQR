package com.example.eventsnapqr;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseController {
    private static FirebaseController instance;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference eventReference = db.collection("events");
    private CollectionReference userReference = db.collection("users");
    private CollectionReference adminReference = db.collection("admin");
    FirebaseController() {

    }

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
    public void addAttendee(String eventIdentifier, Attendee attendee) {
        DocumentReference eventToAttend = eventReference.document(eventIdentifier);
        CollectionReference attendees = eventToAttend.collection("attendees");

        attendees.add(attendee.getDeviceID())
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Attendee document added successfully
                        Log.d("attendee added", "Attendee document added with ID: " + documentReference.getId());
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Failed to add attendee document
                        Log.w("attendee not added", "Error adding attendee document", e);
                    }
                });
        /**
        attendees.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                QuerySnapshot doc = task.getResult();

                List<DocumentSnapshot> attendees = doc.getDocuments();
                // do more
            }
        });
         **/
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

    public void addEvent(Event event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("event name", event.getEventName());
        eventData.put("QR link", event.getQrCode().getLink());
        eventData.put("organizer ID", event.getOrganizer().getDeviceID());
        eventData.put("description", event.getDescription());
        if (event.getPosterUrl() != null) {
            eventData.put("posterURL", event.getPosterUrl());
        }
        if (event.getMaxAttendees() != null) {
            eventData.put("maxAttendees", event.getMaxAttendees());
        }
        if(eventReference != null){
            eventReference.add(eventData).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                @Override
                public void onSuccess(DocumentReference documentReference) {
                    Log.d("Added event succes", "succesfully added event: " + documentReference.getId());

                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("Added event failure", "failed to add event: " + e + Arrays.toString(e.getStackTrace()));
                }
            });
        }


        /**
         attendees.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
        @Override
        public void onComplete(@NonNull Task<QuerySnapshot> task) {
        QuerySnapshot doc = task.getResult();

        List<DocumentSnapshot> attendees = doc.getDocuments();
        // do more
        }
        });
         **/
    }
    /**
    public void addEvent(Event event) {
        eventReference.add()
                .document(event.getQrCode().getLink())
                .set(event)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        Log.d("TAG", "Data has been added successfully!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "Data could not be added!" + e.toString());
                    }
                });
    }
**/
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
                    User user = new User(name, deviceID);
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
                    String eventName = document.getString("event name");
                    String organizerID = document.getString("organizer ID");
                    String qrLink = document.getString("QR link");
                    String description = document.getString("description");
                    String posterUrl = document.getString("posterURL");
                    Integer maxAttendees = document.getLong("maxAttendees") != null ? document.getLong("maxAttendees").intValue() : null;

                    Event event = new Event(new User("", organizerID), new QR(null, qrLink), eventName, description, posterUrl, maxAttendees);
                    listener.onEventRetrieved(event);
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
     * Interface to handle event retrieval.
     */
    public interface OnEventRetrievedListener {
        void onEventRetrieved(Event event);
    }
}
