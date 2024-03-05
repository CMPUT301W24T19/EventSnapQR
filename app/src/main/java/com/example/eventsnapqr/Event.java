package com.example.eventsnapqr;

import android.util.Log;

import java.util.List;

public class Event {
    private String eventName; // name of the event
    private User organizer; // user who organized the event
    private QR qrCode; // qr code for the event
    private String description; // description of the event
    private String posterUrl; // URL for the event poster image
    private Integer maxAttendees; // optional max attendees
    private List<User> signedUpAttendees; // list of users who have signed up
    private List<User> checkedInAttendees; // list of users who are currently checked in
    private FirebaseController firebaseController; // instance of the firebase controller

    public Event() {

    }

    /**
     * constructor for event requiring a user instance, QR code, event name, a description, a URL
     * to the event poster and a max number of attendees. used when all fields are provided
     * @param organizer user who organized the event
     * @param qrCode QR generated for the event
     * @param eventName name of the event
     * @param description description of the event
     * @param posterUrl URL for the event poster
     * @param maxAttendees maximum number of attendees
     */
    public Event(User organizer, QR qrCode, String eventName, String description, String posterUrl, Integer maxAttendees) {
        this.organizer = organizer;
        this.qrCode = qrCode;
        this.eventName = eventName;
        this.description = description;
        this.posterUrl = posterUrl;
        this.maxAttendees = maxAttendees;
        firebaseController = FirebaseController.getInstance();
        firebaseController.addEvent(this);
    }

    /**
     * getter method to return the name of the event
     * @return eventName string
     */
    public String getEventName() {
        return eventName;
    }

    /**
     * getter method to return the device id of the organizing user
     * @return deviceID of the user
     */
    public String getOrganizer() {
        return organizer.getDeviceID();
    }

    /**
     * getter method for the QR code associated with the event
     * @return
     */
    public QR getQrCode() {
        return qrCode;
    }

    /**
     * getter method to return the description of the event as a string
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * getter method to return the URL of the event poster
     * @return poster URL
     */
    public String getPosterUrl() {
        return posterUrl;
    }

    /**
     * setter method for the poster URL
     * @param posterUrl string
     */
    public void setPosterUrl(String posterUrl) {
        this.posterUrl = posterUrl;
    }

    /**
     * getter method to retrieve the optional maximum number of attendees
     * @return maxAttendees
     */
    public Integer getMaxAttendees() {
        return maxAttendees;
    }

    /**
     * setter method to set max number of attendees
     * @param maxAttendees
     */
    public void setMaxAttendees(Integer maxAttendees) {
        this.maxAttendees = maxAttendees;
    }
}
