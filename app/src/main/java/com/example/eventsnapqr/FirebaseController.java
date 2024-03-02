package com.example.eventsnapqr;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

public class FirebaseController {
    private static FirebaseController instance;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference eventReference = db.collection("events");
    private CollectionReference userReference = db.collection("users");
    private FirebaseController() {

    }

    public static synchronized FirebaseController getInstance() {
        if (instance == null) {
            instance = new FirebaseController();
        }
        return instance;
    }

    public void addUser(User user) {
        /*
        String userId = databaseReference.child("users").push().getKey();
        databaseReference.child("users").child(userId).setValue(user);*/
        CollectionReference userReference = db.collection("users");
        userReference
                .document("user name")
                .set(user)
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

    public void addEvent(Event event) {
        eventReference
                .document("event name")
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
}
