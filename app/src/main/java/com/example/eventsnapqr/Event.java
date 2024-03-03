package com.example.eventsnapqr;

import java.util.List;

public class Event {
    private String eventName; // name of the event
    private User organizer; // user who organized the event
    private QR qrCode; // qr code for the event
    private FirebaseController firebaseController;
    private List<User> attendees;

    private String posterUrl; // URL for the event poster image


    public Event() {
        // Default constructor if needed
    }

    public Event(User organizer, QR qrCode, String eventName, String posterUrl) {
        this.organizer = organizer;
        this.qrCode = qrCode;
        this.eventName = eventName;
        this.posterUrl = posterUrl;
        firebaseController = FirebaseController.getInstance();
        firebaseController.addEvent(this);
    }

    // Getter for organizer's name
    public String getOrganizer() {
        return organizer.getUser().getName();
    }

    // Getter for QR code link
    public String getQrCode() {
        return qrCode.getLink();
    }

    // Getter for the poster URL
    public String getPosterUrl() {
        return posterUrl;
    }

    // Setter for the poster resource URL
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }
}
