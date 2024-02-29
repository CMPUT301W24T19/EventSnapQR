package com.example.eventsnapqr;

import android.util.Log;

import java.util.List;

/**
 * an organizer that implements Role meaning it must
 * be assigned as a Role with an associated user. an
 * organizer is creating through the creation of one
 * event, but may host more than one event.
 */
public class Organizer implements Role {
    private User user; // the user associated to this attendee
    private List<Event> organizedEvents;

    /**
     * constructor for organizer that must include a user and
     * the event that resulted in the creation of this Organizer
     * @param user associated user
     * @param organizedEvents list of organized events
     */
    public Organizer(User user) {
        this.user = user;
    }

    /**
     * creates a new event
     * @param qrCode
     */
    private void createEvent(QR qrCode, String eventName){
        Event newEvent = new Event(this, qrCode, eventName);
        organizedEvents.add(newEvent);
    }

    /**
     * getter method for associated user
     * @return this.user the associated user
     */
    public User getUser() {
        return this.user;
    }

    /**
     * getter method for list of events i the
     * organizer have organized
     * @return organized events
     */
    public List<Event> getOrganizedEvents() {
        return organizedEvents;
    }
}
