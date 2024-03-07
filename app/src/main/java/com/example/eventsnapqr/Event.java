package com.example.eventsnapqr;

import android.net.Uri;
import android.util.Log;


import java.util.List;

public class Event {

    private String eventName; // name of the event
    private User organizer; // user who organized the event
    private QR qrCode; // qr code for the event
    private String description; // description of the event
    private String posterUri; // URL for the event poster image
    private Integer maxAttendees; // optional max attendees
    private String eventID;
    private List<User> attendees;
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
     * @param posterUri URL for the event poster
     * @param maxAttendees maximum number of attendees
     */
    public Event(User organizer, QR qrCode, String eventName, String description, String posterUri, Integer maxAttendees, String eventID) {
        this.organizer = organizer;
        this.qrCode = qrCode;
        this.eventName = eventName;
        this.description = description;
        this.posterUri = posterUri;
        this.maxAttendees = maxAttendees;
        this.eventID = eventID;
    }
    public void setQR(QR qrCode){
        this.qrCode = qrCode;
    }
    /**
     * set method to set the description of the event
     * @return eventName string
     */
    public void setDescription(String description) {
        this.description = description;
    }
    /**
     * set method to set the organizer of the event
     * @return eventName string
     */
    public void setOrganizer(User user) {
        this.organizer = user;
    }
    /**
     * getter method to return the name of the event
     * @return eventName string
     */
    public String getEventName() {
        return eventName;
    }
    /**
     * setter method to set the name of the event
     * @return n/a
     */
    public void setEventName(String eventName) {
        this.eventName = eventName;
    }
    /**
     * getter method to return the organizing user
     * @return deviceID of the user
     */
    public User getOrganizer() {
        return organizer;
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
    public String getPosterUri() {
        return posterUri;
    }

    /**
     * setter method for the poster URL
     * @param posterUri string
     */
    public void setPosterUri(String posterUri) {
        this.posterUri = posterUri;
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

    public void addAttendee(User attendee) {
        if (this.maxAttendees != null && this.attendees.size() < this.maxAttendees) {
            this.attendees.add(attendee);
        }
        else {
            throw new IllegalArgumentException();
        }
    }
    public String getEventID() {
        return this.eventID;
    }

    public void setEventID(String eventID) {
        this.eventID = eventID;
    }
}
