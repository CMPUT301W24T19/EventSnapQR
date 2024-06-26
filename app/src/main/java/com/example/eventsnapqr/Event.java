package com.example.eventsnapqr;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * represents an event with a name, organizer, description, poster, maxAttendees (optional), and
 * a announcements string.
 * maxAttendee (optional)
 */
public class Event {
    private String eventName; // name of the event
    private User organizer; // user who organized the event
    private String description; // description of the event
    private String posterURI; // URL for the event poster image
    private Integer maxAttendees; // optional max attendees
    private List<String> announcements; // announcements related to the Event
    private String eventID, address;
    private Date eventStartDateTime, eventEndDateTime;
    private String QR;

    /**
     * constructor for event requiring a user instance, QR code, event name, a description, a URL
     * to the event poster and a max number of attendees. used when all fields are provided
     * @param organizer user who organized the event
     * @param eventName name of the event
     * @param description description of the event
     * @param posterURI URL for the event poster
     * @param maxAttendees maximum number of attendees
     */
    public Event(User organizer, String eventName, String description, String posterURI, Integer maxAttendees,
                 String eventID, Date eventStartDateTime, Date eventEndDateTime, String address, String QR) {
        this.organizer = organizer;
        this.eventName = eventName;
        this.description = description;
        this.posterURI = posterURI;
        this.maxAttendees = maxAttendees;
        this.eventID = eventID;
        this.eventStartDateTime = eventStartDateTime;
        this.eventEndDateTime = eventEndDateTime;
        this.address = address;
        this.QR = QR;
        this.announcements = new ArrayList<>();
    }

    /**
     * empty constructor for firebase usage
     */
    public Event() {
        //empty constructor
    }

    /**
     * Add an announcement to the list of announcements for the event
     * @param announcement The announcement to add
     */
    public void addAnnouncement(String announcement) {
        if (this.announcements == null) {
            this.announcements = new ArrayList<>();
        }
        this.announcements.add(announcement);
    }


    /**
     * setter method to set the current list of announcements
     * @param announcements list of announcements
     */
    public void setAnnouncements(List<String> announcements) {
        this.announcements = announcements;
    }

    /**
     * getter method to return the description of the event as a string
     * @return announcement
     */
    public List<String> getAnnouncements(){return announcements;}

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
    public String getPosterURI() {
        return posterURI;
    }

    /**
     * setter method for the poster URL
     * @param posterURI string
     */
    public void setPosterURI(String posterURI) {
        this.posterURI = posterURI;
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

    /**
     * retrieve the unique ID of the event
     * @return eventID
     */
    public String getEventID() {
        return this.eventID;
    }

    /**
     * get the unique ID of the event
     * @param eventID the new eventID
     */
    public void setEventID(String eventID) {
        this.eventID = eventID;
    }

    /**
     * get the starting date and time of the event
     * @return
     */
    public Date getEventStartDateTime() {
        return this.eventStartDateTime;
    }

    /**
     * get the ending data and time of an event
     * @return
     */
    public Date getEventEndDateTime() {
        return this.eventEndDateTime;
    }

    /**
     * get the address of the event
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * get the QR of the event
     * @return
     */
    public String getQR() {
        return QR;
    }

    /**
     * set the QR of of the event
     */
    public void setQR(String QR) {
        this.QR = QR;
    }
}
