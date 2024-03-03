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
    private String eventName; // name of the event
    private User organizer; // user who organized the event
    private QR qrCode; // qr code for the event
    private FirebaseController firebaseController;
    private List<User> attendees;

    /**
     * Constructor for event using the event name, a user who will be
     * its organizer, and the QR generated/provided for the event
     * @param eventName
     * @param organizer
     * @param qrCode
     */
    public Event(String eventName, User organizer, QR qrCode) {
        //Log.d("TAG", "debug");
        this.eventName = eventName;
        this.organizer = organizer;
        this.qrCode = qrCode;
        firebaseController = FirebaseController.getInstance();
        firebaseController.addEvent(this);
    }

    public String getEventName() {
        return eventName;
    }

    public User getOrganizer() {
        return organizer;
    }

    public String getQrCode() {
        return qrCode.getLink();
    }
}
