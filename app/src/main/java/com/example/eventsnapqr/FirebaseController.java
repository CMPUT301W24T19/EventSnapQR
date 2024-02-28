package com.example.eventsnapqr;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseController {
    private static FirebaseController instance;
    private final DatabaseReference databaseReference;

    private FirebaseController() {
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public static synchronized FirebaseController getInstance() {
        if (instance == null) {
            instance = new FirebaseController();
        }
        return instance;
    }

    public DatabaseReference getDatabaseReference() {
        return databaseReference;
    }
    public void addUser(User user) {
        String userId = databaseReference.child("users").push().getKey();
        databaseReference.child("users").child(userId).setValue(user);
    }
}
