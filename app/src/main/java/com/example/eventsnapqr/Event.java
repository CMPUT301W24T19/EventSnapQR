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
    private FirebaseController firebaseController;


    private String eventName;

    // private EventPoster poster;
    private List<Attendee> attendees;
    private FirebaseController controller;
    public Event() {
    }

    public Event(Organizer organizer, QR qrCode, String eventName) {
        //Log.d("TAG", "debug");
        this.qrCode = qrCode;
        this.organizer = organizer;
        this.eventName = eventName;
        firebaseController = FirebaseController.getInstance();
        firebaseController.addEvent(this);
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
