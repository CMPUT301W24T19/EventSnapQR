package com.example.eventsnapqr;

import android.util.Log;

import java.util.List;

public class Event {
<<<<<<< HEAD
<<<<<<< HEAD
    private String eventName;
    private User organizer;
    private QR qrCode;
    private FirebaseController firebaseController;
    private List<User> attendees;

    private String posterUrl;

=======
=======

>>>>>>> c5dc6a338ea97803f0c0920578be5b790516fddf
    private String eventName; // name of the event
    private User organizer; // user who organized the event
    private QR qrCode; // qr code for the event
    private String description; // description of the event
    private String posterUrl; // URL for the event poster image
    private Integer maxAttendees; // optional max attendees
    private List<User> signedUpAttendees; // list of users who have signed up
    private List<User> checkedInAttendees; // list of users who are currently checked in
    private FirebaseController firebaseController; // instance of the firebase controller
<<<<<<< HEAD
>>>>>>> 9af50ec93a9e0c955be995986e1d5a5d14bac95b
=======

>>>>>>> c5dc6a338ea97803f0c0920578be5b790516fddf

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
