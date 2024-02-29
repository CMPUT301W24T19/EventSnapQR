package com.example.eventsnapqr;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Event {
    private Organizer organizer;
    private QR qrCode;


    private String eventName;

    // private EventPoster poster;
    private List<Attendee> attendees;
    private FirebaseController controller;
    public Event() {
    }

    public Event(Organizer organizer, QR qrCode, String eventName) {
        Log.d("TAG", "debug");
        this.qrCode = qrCode;
        this.organizer = organizer;
        this.eventName = eventName;
        FirebaseFirestore db;
        db = FirebaseFirestore.getInstance();
        final CollectionReference eventReference = db.collection("events");

        eventReference
                .document("event name")
                .set(this)
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

    public String getOrganizer() {
        return organizer.getUser().getName();
    }
    public String getQrCode() {
        return qrCode.getLink();
    }

    /**
    private void setPoster(EventPoster poster){
        this.poster = poster;
    }
    **/
}
