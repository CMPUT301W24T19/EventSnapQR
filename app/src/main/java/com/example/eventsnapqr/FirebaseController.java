package com.example.eventsnapqr;

import static androidx.camera.core.CameraXThreads.TAG;

import com.google.firebase.firestore.DocumentChange;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.TaskCompletionSource;
import com.google.android.gms.tasks.Tasks;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;

/**
 * class to interact with the collections in the firestore database
 */
public class FirebaseController {
    private static FirebaseController instance;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference eventReference = db.collection("events");
    private CollectionReference userReference = db.collection("users");
    private double latitude = 0.0;
    private double longitude = 0.0;

    FirebaseController() {}

    public static synchronized FirebaseController getInstance() {
        if (instance == null) {
            instance = new FirebaseController();
        }
        return instance;
    }

    /**
     * method that uses a listener to return whether a given androidId has a
     * matching user in the database
     * @param androidId ID to check
     * @param listener interface to help return result
     */
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

    /**
     * listener for checkUserExists
     */
    public interface Authenticator {
        void onUserExistenceChecked(boolean exists);
        void onAdminExistenceChecked(boolean exists);
    }

    /**
     * initiates the user deletion process
     * @param user
     */
    public void deleteUser(User user) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String userId = user.getDeviceID();

        deleteOrganizedEvents(db, userId, () -> {
            deleteUserFinalStep(db, userId, new UserDeletedCallback() {
                @Override
                public void userDeleted() {
                    // do nothing
                }
            });
        });
    }

    /**
     * deletes all the organized events of a user
     * @param db instance of database
     * @param userId given unique user identifier
     * @param callback used to execute follow up actions
     */
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

    /**
     * callback listener for
     */
    interface UserDeletedCallback{
        void userDeleted();
    }
    /**
     *
     * @param db
     * @param userId
     */
    public void deleteUserFinalStep(FirebaseFirestore db, String userId, UserDeletedCallback callback) {
        CollectionReference notificationReference = db.collection("users").document(userId).collection("notifications");
        notificationReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot snapshot = task.getResult();
                    if (snapshot.size() > 0) {
                        int[] i = {0};
                        for (QueryDocumentSnapshot doc : snapshot) {
                            notificationReference.document(doc.getId()).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (i[0] == snapshot.size() - 1) {
                                        db.collection("users").document(userId).delete()
                                                .addOnSuccessListener(aVoid -> {Log.d("Delete User", "User successfully deleted: " + userId); callback.userDeleted();})
                                                .addOnFailureListener(e -> Log.e("Delete User", "Error deleting user: " + userId, e));
                                    }
                                    i[0]++;
                                }
                            });
                        }
                    }
                    else {
                        db.collection("users").document(userId).delete()
                                .addOnSuccessListener(aVoid -> {Log.d("Delete User", "User successfully deleted: " + userId); callback.userDeleted();})
                                .addOnFailureListener(e -> Log.e("Delete User", "Error deleting user: " + userId, e));
                    }
                } else {
                    db.collection("users").document(userId).delete()
                            .addOnSuccessListener(aVoid -> {Log.d("Delete User", "User successfully deleted: " + userId); callback.userDeleted();})
                            .addOnFailureListener(e -> Log.e("Delete User", "Error deleting user: " + userId, e));
                }
            }
        });
    }

    /**
     *
     * @param db
     * @param eventId
     * @return
     */
    private Task<Void> fetchAndDeleteEvent(FirebaseFirestore db, String eventId) {
        TaskCompletionSource<Void> taskCompletionSource = new TaskCompletionSource<>();

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Event event = documentSnapshot.toObject(Event.class);
                        String userID = documentSnapshot.getString("organizerID");
                        User user = new User(userID, userID, null, null, null);
                        event.setOrganizer(user);
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

    public interface FirestoreOperationCallback {
        void onCompleted();
    }


    /**
     * deletes an event from the firestore database, and ensures data is consistent when this
     * event is removed.
     * @param event
     * @param completionCallback
     */
    public void deleteEvent(Event event, FirestoreOperationCallback completionCallback) {
        String eventId = event.getEventID();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        if (event.getPosterURI() != null) {
            deleteImage(event.getPosterURI(), event, null, true);
        }

        // deleting subcollections
        Task<QuerySnapshot> getMilestones = db.collection("events").document(eventId).collection("milestones").get();
        Task<QuerySnapshot> getAttendees = db.collection("events").document(eventId).collection("attendees").get();
        Task<QuerySnapshot> getPromisedAttendees = db.collection("events").document(eventId).collection("promisedAttendees").get();
        Task<QuerySnapshot> getAnnouncements = db.collection("events").document(eventId).collection("announcements").get();

        Task<QuerySnapshot> getUsersTask = db.collection("users").get();

        Tasks.whenAll(getMilestones, getAttendees, getPromisedAttendees, getAnnouncements, getUsersTask).addOnSuccessListener(aVoid -> {

            List<Task<Void>> deletionTasks = new ArrayList<>();
            for (DocumentSnapshot userDoc : getUsersTask.getResult().getDocuments()) {
                String userId = userDoc.getId();
                Task<Void> deleteOrganizedEventTask = db.collection("users").document(userId).collection("organizedEvents").document(eventId).delete();
                Task<Void> deletePromisedEventTask = db.collection("users").document(userId).collection("promisedEvents").document(eventId).delete();
                deletionTasks.add(deleteOrganizedEventTask);
                deletionTasks.add(deletePromisedEventTask);
            }

            for (DocumentSnapshot milestoneDoc : getMilestones.getResult().getDocuments()) {
                Task<Void> deleteMileStoneTask = db.collection("events").document(eventId).collection("milestones").document(milestoneDoc.getId()).delete();
                deletionTasks.add(deleteMileStoneTask);
            }

            for (DocumentSnapshot attendeeDoc : getAttendees.getResult().getDocuments()) {
                Task<Void> deleteAttendeeTask = db.collection("events").document(eventId).collection("attendees").document(attendeeDoc.getId()).delete();
                deletionTasks.add(deleteAttendeeTask);
            }

            for (DocumentSnapshot attendeeDoc : getPromisedAttendees.getResult().getDocuments()) {
                Task<Void> deletePromisedAttendeeTask = db.collection("events").document(eventId).collection("promisedAttendees").document(attendeeDoc.getId()).delete();
                deletionTasks.add(deletePromisedAttendeeTask);
            }

            for (DocumentSnapshot announcementDoc : getAnnouncements.getResult().getDocuments()) {
                Task<Void> deleteAnnouncementTask = db.collection("events").document(eventId).collection("announcements").document(announcementDoc.getId()).delete();
                deletionTasks.add(deleteAnnouncementTask);
            }

            Tasks.whenAllSuccess(deletionTasks).addOnSuccessListener(tasks -> {
                Log.d("Delete Event", "Event and related data successfully deleted: " + eventId);
                db.collection("events").document(eventId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (completionCallback != null) {
                            completionCallback.onCompleted();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "Event deletion failure");
                        if (completionCallback != null) {
                            completionCallback.onCompleted();
                        }
                    }
                });
            }).addOnFailureListener(e -> {
                Log.e("Delete Event", "Error deleting event or related data: " + eventId, e);
                db.collection("events").document(eventId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        if (completionCallback != null) {
                            completionCallback.onCompleted();
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "Event deletion failure");
                        if (completionCallback != null) {
                            completionCallback.onCompleted();
                        }
                    }
                });
            });
        }).addOnFailureListener(e -> {
            Log.e("Delete Event", "Error initializing deletion process: " + eventId, e);
            db.collection("events").document(eventId).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    if (completionCallback != null) {
                        completionCallback.onCompleted();
                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.d("TAG", "Event deletion failure");
                    if (completionCallback != null) {
                        completionCallback.onCompleted();
                    }
                }
            });
        });
    }

    /**
     * adds a given user to the firestore database
     * @param user user object
     */
    public void addUser(User user, Runnable callback) {
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
                        if (callback != null) {
                            callback.run();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Added user failure", "Failed to add user: " + e);
                    }
                });
    }

    /**
     * uses an interface to return a list of all the attendees in an event
     * @param event
     * @param callback
     */
    public void getEventAttendees(Event event, User.AttendeesCallback callback) {
        db.collection("events").document(event.getEventID()).collection("attendees").get()
                .addOnSuccessListener(querySnapshot -> {
                    List<String> attendees = new ArrayList<>();
                    for (DocumentSnapshot document : querySnapshot) {
                        String androidId = document.getString("organizerID");
                        attendees.add(androidId);
                    }
                    callback.onAttendeesLoaded(attendees); // Pass the attendees list to the callback
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                });
    }

    /**
     * Gets all the event documents, then turns each event document into an event object and adds it to an array
     * @param documents, a list of event documents
     */
    void parseDocuments(List<DocumentSnapshot> documents) {
        for(DocumentSnapshot doc: documents){

            Event event = new Event();
            event.setEventID(doc.getId());
            event.setOrganizer(new User(doc.getString("organizerID")));
            //doc.get("attendees");
            event.setDescription(doc.getString("description"));
            event.setEventName(doc.getString("eventName"));
            event.setPosterURI(doc.getString("posterURL"));
            event.setQR(doc.getString("QR"));
            events.add(event);
            //Event(User organizer, QR qrCode, String eventName, String description, String posterUrl, Integer maxAttendees)
        }
    }
    public interface OnEventsLoadedListener {
        void onEventsLoaded(ArrayList<Event> events);
    }
    ArrayList<Event> events = new ArrayList<>();

    /**
     * Gets all the event documents, stores them into an array and add a callback with the new array
     * @param listener, callback for when all event documents are converted into objects and stored into an array
     */
    public void getAllEvents(final OnEventsLoadedListener listener) {
        eventReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    events.clear(); // Clear the events list before adding new data
                    parseDocuments(queryDocumentSnapshots.getDocuments());
                    listener.onEventsLoaded(events);
                }
                else {
                    listener.onEventsLoaded(new ArrayList<Event>());
                }

            }
        });
    }

    /**
     * used to retrieve the unique id generated by firestore for an event
     * @return
     */
    public String getUniqueEventID() {
        DocumentReference addedDocRef = eventReference.document();
        return addedDocRef.getId();
    }

    /**
     * adds an event and its fields to the firestore database
     * @param event The event to add
     */
    public void addEvent(Event event) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("eventName", event.getEventName());
        eventData.put("organizerID", event.getOrganizer().getDeviceID());
        eventData.put("description", event.getDescription());
        eventData.put("startDateTime", event.getEventStartDateTime());
        eventData.put("endDateTime", event.getEventEndDateTime());
        eventData.put("QR", event.getQR());
        eventData.put("address", event.getAddress());

        if (event.getPosterURI() != null) {
            eventData.put("posterURI", event.getPosterURI());
        }
        if (event.getMaxAttendees() != null) {
            eventData.put("maxAttendees", event.getMaxAttendees());
        }

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
    /**
     * Gets all the user documents, stores them into an array and add a callback with the new array
     * @param listener, callback for when all user documents are converted into objects and stored into an array
     */
    public void getAllUsers(OnAllUsersLoadedListener listener){
        userReference.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                if (!queryDocumentSnapshots.isEmpty()) {
                    users.clear();
                    parseUsers(queryDocumentSnapshots.getDocuments());
                    listener.onUsersLoaded(users);
                }
                else {
                    listener.onUsersLoaded(new ArrayList<User>());
                }

            }
        });

    }

    /**
     * Gets all the user documents, then turns each user document into a user object and adds it to an array
     * @param documents, a list of users documents
     */
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
    private void makeNotification(Context context, String announcement, Event event) {
        Intent intent = new Intent(context, BrowseEventsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("eventID", event.getEventID());
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_MUTABLE);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CHANNEL_ID_NOTIFICATION");
        builder.setContentTitle("Notification from " + event.getEventName())
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentText(announcement)
                .setSmallIcon(R.drawable.baseline_notifications_24).setContentIntent(pendingIntent);

        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = notificationManager.getNotificationChannel("CHANNEL_ID_NOTIFICATION");
            if(notificationChannel == null){
                notificationChannel = new NotificationChannel("CHANNEL_ID_NOTIFICATION", event.getEventName() + " notification",NotificationManager.IMPORTANCE_HIGH);
                notificationChannel.setLightColor(Color.GREEN);
                notificationChannel.enableVibration(true);
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }
        notificationManager.notify(0, builder.build());
    }
    public void isAttendee(String androidId, Event event, AttendeeCheckCallback callback){
        db.collection("events").document(event.getEventID()).collection("attendees").get()
                .addOnSuccessListener(querySnapshot -> {
                    boolean found = false;
                    for (DocumentSnapshot document : querySnapshot) {
                        if(document.getId().equals(androidId)){
                            found = true;
                            break;
                        }
                    }
                    callback.onChecked(found, event);
                })
                .addOnFailureListener(e -> {
                    callback.onChecked(false, null);
                });
    }
    public interface AttendeeCheckCallback {
        void onChecked(boolean isAttendee, Event event);
    }

    public interface NotificationSeenCallback {
        void onSeen(boolean seen);
    }
    public void listenForAnnouncements(Context context, Event event) {
        if (event == null || event.getEventID() == null) {
            Log.e("FirebaseController", "Event or Event ID is null");
            return;
        }

        CollectionReference announcementsRef = db.collection("events").document(event.getEventID()).collection("announcements");

        announcementsRef.orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @SuppressLint("HardwareIds")
                    @Override
                    public void onEvent(@Nullable QuerySnapshot snapshots,
                                        @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Log.w("ListenFailed", e);
                            return;
                        }

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            switch (dc.getType()) {
                                case ADDED:
                                    String announcementID = dc.getDocument().getId();
                                    ContentResolver contentResolver = context.getContentResolver();
                                    Boolean toNotify = (Boolean)dc.getDocument().get("notify");

                                    if(toNotify){
                                        markSeenNotification(announcementID, Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID), new NotificationSeenCallback() {
                                            @Override
                                            public void onSeen(boolean seen) {
                                                if(!seen){
                                                    if(!event.getOrganizer().getDeviceID().equals(Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID))){
                                                        String announcementMessage = dc.getDocument().getString("message");
                                                        makeNotification(context, announcementMessage, event);
                                                    }

                                                }
                                            }
                                        });
                                    }else{
                                        markSeenNotification(announcementID, Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID), new NotificationSeenCallback() {
                                            @Override
                                            public void onSeen(boolean seen) {
                                                // Do nothing but we still meed to call mark seen
                                            }
                                        });
                                    }

                                    break;
                                case MODIFIED:
                                case REMOVED:
                                    break;
                            }
                        }
                    }
                });
    }

    /**
     *
     * @param announcementID unique id of the announcement that the notification is attached to
     * @param userID the userid receiving the notifications
     * @param notificationSeenCallback
     */
    private void markSeenNotification(String announcementID, String userID, NotificationSeenCallback notificationSeenCallback) {
        CollectionReference notificationsRef = db.collection("users").document(userID).collection("notifications");

        // check if the notification document exists
        notificationsRef.document(announcementID).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    // the document exists
                    notificationSeenCallback.onSeen(true);
                    Log.d("markSeenNotification", "User already seen notification.");
                } else {
                    // the document does not exist, add it to the collection
                    Map<String, Object> notificationData = new HashMap<>();
                    notificationData.put("seen", true);
                    notificationSeenCallback.onSeen(false);
                    notificationsRef.document(announcementID).set(notificationData)
                            .addOnSuccessListener(aVoid -> Log.d("markSeenNotification", "Document added successfully."))
                            .addOnFailureListener(e -> Log.e("markSeenNotification", "Error adding document", e));
                }
            } else {
                Log.e("markSeenNotification", "Failed to check document existence: ", task.getException());
            }
        });
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
                    String email = document.getString("email");
                    String homePage = document.getString("homepage");
                    String phoneNumber = document.getString("phoneNumber");
                    User user = new User(name, deviceID, homePage, phoneNumber, email);
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

    /**
     * called with get user to return the user object
     */
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
                    String description = document.getString("description");
                    String posterUri = document.getString("posterURI");
                    Date startDateTime = document.getDate("startDateTime");
                    Date endDateTime = document.getDate("endDateTime");
                    String address = document.getString("address");
                    String eventId = eventRef.getId();
                    String QR = document.getString("QR");
                    Integer maxAttendees = document.getLong("maxAttendees") != null ? document.getLong("maxAttendees").intValue() : null;

                    db.collection("events").document(eventIdentifier).collection("announcements")
                            .get()
                            .addOnCompleteListener(subCollectionTask -> {
                                if (subCollectionTask.isSuccessful()) {
                                    List<String> announcements = new ArrayList<>();
                                    for (QueryDocumentSnapshot announcementDoc : subCollectionTask.getResult()) {
                                        String message = announcementDoc.getString("message");
                                        if (message != null) {
                                            announcements.add(message);
                                        }
                                    }

                                    getUser(organizerID, new OnUserRetrievedListener() {
                                        @Override
                                        public void onUserRetrieved(User user) {
                                            if (user != null) {
                                                Event event = new Event(user, eventName, description, posterUri, maxAttendees, eventId, startDateTime, endDateTime, address, QR);
                                                event.setAnnouncements(announcements);
                                                listener.onEventRetrieved(event);
                                            } else {
                                                Log.d("Error", "Failed to retrieve organizer details for event: " + eventIdentifier);
                                                listener.onEventRetrieved(null);
                                            }
                                        }
                                    });
                                } else {
                                    Log.d("Error", "Error getting announcements subcollection: " + subCollectionTask.getException());
                                    listener.onEventRetrieved(null);
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
        DocumentReference eventRef = eventReference.document(event.getEventID());
        // added add user to events promised attendees list aswell
        eventRef.collection("promisedAttendees").document(user.getDeviceID()).set(new HashMap<>()).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Added user to event promised attendees",
                                "User added to promised attendees subcollection for event: " + user.getDeviceID());
                        eventRef.collection("promisedAttendees").get()
                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                    @Override
                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                        int count = queryDocumentSnapshots.size();
                                        if(count == 1){
                                            addMilestone(event, "First attendee has signed for your event: " + user.getName());
                                        }
                                        if(count == 5){
                                            addMilestone(event, "5 users have signed up for your event!");
                                        }
                                        if(count%10 == 0){
                                            addMilestone(event, count+ " users have signed up for your event!");
                                        }
                                        Log.d("Count of promised attendees", "Number of signed up attendees for event: " + count);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Error getting promised attendees count", "Error fetching promised attendees for event");
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Failed to add to users promised events",
                                "Failed to add event to event promised users subcollection for event: " + event.getEventID());
                    }
                });
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
     * add a string milestone to an events milestone collection in Firestone
     * @param event the event in question
     * @param milestone the string in question
     */
    public void addMilestone(Event event, String milestone) {
        DocumentReference eventRef = eventReference.document(event.getEventID());
        final CollectionReference collectionReference = eventRef.collection("milestones");
        final Map<String, Object> milestoneData = new HashMap<>();
        collectionReference.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    QuerySnapshot querySnapshot = task.getResult();
                    if (querySnapshot != null && !querySnapshot.isEmpty()) {
                        // The "milestones" collection exists, so add a new document to it
                        collectionReference.document(milestone).set(milestoneData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Added milestone", "Milestone added: " + milestone);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Failed to add milestone", "Failed to add milestone: " + milestone);
                                    }
                                });
                    } else {
                        // The "milestones" collection doesn't exist, so create it and add a new document
                        collectionReference.document(milestone).set(milestoneData)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("Added milestone", "Milestone added: " + milestone);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.d("Failed to add milestone", "Failed to add milestone: " + milestone);
                                    }
                                });
                    }
                } else {
                    Log.d("Query failed", "Error getting documents: ", task.getException());
                }
            }
        });
    }

    /**
     * Listener that returns a list of milestone strings upon successful retrieval
     */
    public interface MilestonesListener {
        void onMilestonesLoaded(List<String> milestones);
    }

    /**
     * retrieve all the event milestones of a given event
     * @param eventId event in question
     * @param listener MilestonesListener
     */
    public void getMilestones(String eventId, MilestonesListener listener) {
        DocumentReference eventRef = eventReference.document(eventId);
        CollectionReference milestonesRef = eventRef.collection("milestones");
        milestonesRef.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    List<String> milestonesList = new ArrayList<>();
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String milestone = document.getId();
                        milestonesList.add(milestone);
                    }
                    listener.onMilestonesLoaded(milestonesList);
                } else {
                    Log.d("Get milestones failed", "Error getting documents: ", task.getException());
                }
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
        attendeeData.put("checkedIn", 0);
        attendeeData.put("latitude","");
        attendeeData.put("longitude","");
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

    /**
     * returns the incremented value after check-in
     */
    public interface CheckInListener {
        void onCheckInComplete(int count);
        void onCheckInFailure(Exception e);
    }

    /**
     * increment the number of times an attendee has checked into an event,
     * and return the result
     * @param userId given user to increment
     * @param eventId the event in which the user checked in
     * @param listener acts as a listener to return the integer result
     */
    public void incrementCheckIn(String userId, String eventId, CheckInListener listener) {
        DocumentReference eventRef = db.collection("events").document(eventId)
                .collection("attendees").document(userId);

        eventRef.update("checkedIn", FieldValue.increment(1))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        eventRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                            @Override
                            public void onSuccess(DocumentSnapshot documentSnapshot) {
                                Integer updatedCount = documentSnapshot.getLong("checkedIn").intValue(); // Convert Long to Integer and then to int
                                if (updatedCount != null) {
                                    listener.onCheckInComplete(updatedCount);
                                    if(updatedCount == 2){
                                        FirebaseController fb = new FirebaseController();
                                        fb.getEvent(eventId, new OnEventRetrievedListener() {
                                            @Override
                                            public void onEventRetrieved(Event event) {
                                                addMilestone(event, "User with ID: " + userId + " has checked into your event more than once!");
                                            }
                                        });
                                    }

                                } else {
                                    listener.onCheckInFailure(new RuntimeException("Failed to retrieve updated count"));
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                listener.onCheckInFailure(e);
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onCheckInFailure(e);
                    }
                });
    }

    /**
     * entirely deletes an image from Firestore and all necessary documents.
     * @param uri the image in question
     * @param object event or user with an image URI
     * @param context activity/fragment context
     */
    public void deleteImage(String uri, Object object, Context context, boolean deleteObject) {
        String[] firebaseImagePath = Uri.parse(uri).getPath().split("/");
        String imagePath = firebaseImagePath[firebaseImagePath.length - 2] + "/" + firebaseImagePath[firebaseImagePath.length - 1];
        FirebaseStorage.getInstance().getReference().child(imagePath).delete().addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) { // after deleting from storage, delete from event and uri documents
                Log.d("TAG", "Picture successfully deleted");
                if (!deleteObject) {
                    if (object instanceof Event) {
                        Event event = (Event) object;
                        String eventId = event.getEventID();
                        if (eventId != null) {
                            FirebaseFirestore.getInstance().collection("events").document(eventId)
                                    .update("posterURI", null)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("TAG", "Event poster URI set to null");
                                            Toast.makeText(context, "Image removed successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("TAG", "Failed to update event poster URI", e);
                                            Toast.makeText(context, "Failed to remove image", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    } else if (object instanceof User) {
                        User user = (User) object;
                        String userId = user.getDeviceID();
                        if (userId != null) {
                            FirebaseFirestore.getInstance().collection("users").document(userId)
                                    .update("profileURI", null)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d("TAG", "User profile URI set to null");
                                            Toast.makeText(context, "Image removed successfully", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Log.e("TAG", "Failed to update user profile URI", e);
                                            Toast.makeText(context, "Failed to remove image", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                        }
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d("TAG", "Picture not deleted");
                Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * checks if a user is signed up to a given event or not
     * @param userId user in question
     * @param eventId event in question
     * @param listener OnSignUpCheckListener
     */
    public void isUserSignedUp(String userId, String eventId, OnSignUpCheckListener listener) {
        FirebaseFirestore.getInstance()
                .collection("events")
                .document(eventId)
                .collection("attendees")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    boolean isSignedUp = documentSnapshot.exists();
                    listener.onSignUpCheck(isSignedUp);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseController", "Error checking user signup", e);
                    listener.onSignUpCheck(false);
                });
    }

    /**
     * listener for isUserSignedUp so it can return a boolean value
     */
    public interface OnSignUpCheckListener {
        void onSignUpCheck(boolean isSignedUp);
    }


    /**
     * removes an attendee from an event in all necessary collections
     * @param eventId event in question
     * @param userId user in question
     * @param callback Listener for isUserSignedUp so it can return a boolean value
     */
    public void removeAttendee(String eventId, String userId, RemoveAttendeeCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference attendeeRef = db.collection("events").document(eventId).collection("attendees").document(userId);
        DocumentReference promisedEventRef = db.collection("users").document(userId).collection("promisedEvents").document(eventId);

        attendeeRef.delete().addOnCompleteListener(attendeeDeleteTask -> {
            if (attendeeDeleteTask.isSuccessful()) {
                promisedEventRef.delete().addOnCompleteListener(promisedEventDeleteTask -> {
                    if (promisedEventDeleteTask.isSuccessful()) {
                        if (callback != null) callback.onSuccess();
                    } else if (callback != null) callback.onFailure(promisedEventDeleteTask.getException());
                });
            } else if (callback != null) callback.onFailure(attendeeDeleteTask.getException());
        });
    }

    /**
     * callback for removeAttendees denoting successful removal
     */
    public interface RemoveAttendeeCallback {
        void onSuccess();
        void onFailure(Exception e);
    }

    /**
     * get the number of times given user has checked in to a given event, -1 otherwise
     * @param eventId event in question
     * @param userId user in question
     * @param callback CheckAttendeeCheckinsCallback
     */
    public void checkAttendeeCheckins(String eventId, String userId, CheckAttendeeCheckinsCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference attendeeRef = db.collection("events").document(eventId)
                .collection("attendees").document(userId);

        attendeeRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Number checkinsNumber = document.getLong("checkedIn");
                    if (checkinsNumber != null) {
                        callback.onSuccess(checkinsNumber.intValue());
                    } else {
                        callback.onSuccess(-1);
                    }
                } else {
                    callback.onSuccess(-1);
                }
            } else {
                callback.onFailure(task.getException());
            }
        });
    }

    /**
     * listener for isUserSignedUp so it can return an int
     */
    public interface CheckAttendeeCheckinsCallback {
        void onSuccess(int checkins);
        void onFailure(Exception e);
    }
}
