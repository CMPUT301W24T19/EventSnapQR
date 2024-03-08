package com.example.eventsnapqr;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

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
                listener.onUserExistenceChecked(false);
            }
        });
    }
    public interface Authenticator {
        void onUserExistenceChecked(boolean exists);
        void onAdminExistenceChecked(boolean exists);
    }

    public void deleteUser(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getDeviceID();

        deleteOrganizedEvents(db, userId, () -> {
            deleteUserFinalStep(db, userId);
        });
    }

    public interface FirestoreOperationCallback {
        void onCompleted();
    }

    private void deleteOrganizedEvents(FirebaseFirestore db, String userId, Runnable callback){
        db.collection("users").document(userId).collection("organizedEvents")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        List<Task<Void>> deleteTasks = new ArrayList<>();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String eventId = document.getId();
                            Task<Void> deleteTask = fetchAndDeleteEvent(db, eventId);
                            deleteTasks.add(deleteTask);
                        }

                        Tasks.whenAllComplete(deleteTasks).addOnCompleteListener(tasks -> {
                            callback.run();
                        });
                    } else {
                        Log.e("Delete Organized Events", "Error fetching organized events for user: " + userId, task.getException());
                        callback.run();
                    }
                });
    }

    private void deleteUserFinalStep(FirebaseFirestore db, String userId) {
        db.collection("users").document(userId).delete()
                .addOnSuccessListener(aVoid -> Log.d("Delete User", "User successfully deleted: " + userId))
                .addOnFailureListener(e -> Log.e("Delete User", "Error deleting user: " + userId, e));
    }

    private Task<Void> fetchAndDeleteEvent(FirebaseFirestore db, String eventId) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        if (event != null) {
                            event.setEventID(documentSnapshot.getId());
                            deleteEvent(event, () -> {
                                taskCompletionSource.setResult(null);
                            });
                        } else {
                            taskCompletionSource.setResult(null);
                        }
                    } else {
                        taskCompletionSource.setResult(null);
                    }
                })
                .addOnFailureListener(e -> {
                    taskCompletionSource.setException(e);
                });

        return taskCompletionSource.getTask();
    }


    public void deleteEvent(Event event, FirestoreOperationCallback completionCallback) {
        String eventId = event.getEventID();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Task<Void> deleteEventTask = db.collection("events").document(eventId).delete();

        Task<QuerySnapshot> getUsersTask = db.collection("users").get();

        Tasks.whenAll(deleteEventTask, getUsersTask).addOnSuccessListener(aVoid -> {
            List<Task<Void>> deletionTasks = new ArrayList<>();
            for (DocumentSnapshot userDoc : getUsersTask.getResult().getDocuments()) {
                String userId = userDoc.getId();
                Task<Void> deleteOrganizedEventTask = db.collection("users").document(userId).collection("organizedEvents").document(eventId).delete();
                Task<Void> deletePromisedEventTask = db.collection("users").document(userId).collection("promisedEvents").document(eventId).delete();
                deletionTasks.add(deleteOrganizedEventTask);
                deletionTasks.add(deletePromisedEventTask);
            }

            Tasks.whenAllSuccess(deletionTasks).addOnSuccessListener(tasks -> {
                Log.d("Delete Event", "Event and related data successfully deleted: " + eventId);
                if (completionCallback != null) {
                    completionCallback.onCompleted();
                }
            }).addOnFailureListener(e -> {
                Log.e("Delete Event", "Error deleting event or related data: " + eventId, e);
                if (completionCallback != null) {
                    completionCallback.onCompleted();
                }
            });
        }).addOnFailureListener(e -> {
            Log.e("Delete Event", "Error initializing deletion process: " + eventId, e);
            if (completionCallback != null) {
                completionCallback.onCompleted();
            }
        });
    }


    private void removeFromUsersCollections(FirebaseFirestore db, String eventId) {
        db.collection("users").get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String userId = documentSnapshot.getId();
                        db.collection("users").document(userId).collection("organizedEvents").document(eventId).delete();
                        db.collection("users").document(userId).collection("promisedEvents").document(eventId).delete();
                        Log.d("Remove Event from User", "Removed event " + eventId + " from user: " + userId);
                    }
                })
                .addOnFailureListener(e -> Log.e("Remove Event from Users", "Error removing event from users", e));
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
    public void getEventAttendees(Event event, User.AttendeesCallback callback) {
        db.collection("events").document(event.getEventID()).collection("attendees").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> attendees = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        String androidId = document.getString("organizerID");
                        attendees.add(androidId);
                    }
                    callback.onAttendeesLoaded(attendees); // Pass the attendees list to the callback
                });
    }


    void parseDocuments(List<DocumentSnapshot> documents) {
        for(DocumentSnapshot doc: documents){
            Event event = new Event();
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
                                Event event = new Event(user, eventName, description, posterUri, maxAttendees, eventId, announcement);
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

    /**
     * check if the user is in the attendees list of a specific event.
     *
     * @param eventId   The ID of the event to check.
     * @param userId    The ID of the user to check.
     * @param listener  Listener to handle the result of the check.
     */
    public void checkUserInAttendees(String eventId, String userId, OnUserInAttendeesListener listener) {
        DocumentReference eventRef = db.collection("events").document(eventId);
        eventRef.collection("attendees").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            listener.onUserInAttendees(true);
                        } else {
                            listener.onUserInAttendees(false);
                        }
                    } else {
                        listener.onCheckFailed(task.getException());
                    }
                });
    }

    /**
     * interface to handle the result of checking if a user is in the attendees list.
     */
    public interface OnUserInAttendeesListener {
        void onUserInAttendees(boolean isInAttendees);
        void onCheckFailed(Exception e);
    }

    public void incrementCheckIn(String userId, String eventId) {
        DocumentReference eventRef = db.collection("events").document(eventId)
                .collection("attendees").document(userId);

        eventRef.update("checkedIn", FieldValue.increment(1))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Increment Check-In",
                                "Successfully incremented check-in count for user " + userId + " in event " + eventId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Increment Check-In", "Failed to increment check-in count: " + e.getMessage());
                    }
                });
    }

}
