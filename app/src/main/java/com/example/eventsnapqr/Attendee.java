package com.example.eventsnapqr;

import java.util.ArrayList;
import java.util.List;

/**
 * an attendee that implements role indicating it
 * must have a user associated with it. an attendee has
 * list of events that they are currently attending and
 * will be attending in the future
 */
public class Attendee implements Role {
    private User user; // the user associated to this attendee
    private List<Event> currentEvents; // list of current events
    private List<Event> futureEvents; // list of future events

    /**
     * constructor that requires only a user to instantiate
     * @param user the user associated with this attendee
     */
    public Attendee(User user) {
        this.user = user;
    }

    /**
     * constructor that can also take in a users scheduled events
     * @param user the user associated with this attendee
     * @param currentEvents the users events in progress
     * @param futureEvents the users upcoming events
     */
    public Attendee(User user, List<Event> currentEvents, List<Event> futureEvents) {
        this.user = user;
        this.currentEvents = currentEvents;
        this.futureEvents = futureEvents;
    }

    /**
     * getter method for the associated user
     * @return associated user
     */
    public User getUser() {
        return this.user;
    }

    /**
     * getter method for list of current events
     * @return current events
     */
    public List<Event> getCurrentEvents() {
        return currentEvents;
    }

    /**
     * getter method for list of future events
     * @return future events
     */
    public List<Event> getFutureEvents() {
        return futureEvents;
    }

    /**
     * add events to the currentEvents list
     * @param newCurrentEvent new event
     */
    public void addCurrentEvents(Event newCurrentEvent) {
        currentEvents.add(newCurrentEvent);
    }

    /**
     * remove events from the currentEvents list
     * @param currentEvent event to be removed
     */
    public void removeCurrentEvents(Event currentEvent) {
        currentEvents.remove(currentEvent);
    }

    /**
     * add events to the futureEventsList
     * @param newCurrentEvent new event
     */
    public void addFutureEvents(Event newCurrentEvent) {
        futureEvents.add(newCurrentEvent);
    }

    /**
     * remove events from the futureEvents list
     * @param currentEvent event to be removed
     */
    public void removeFutureEvents(Event currentEvent) {
        futureEvents.remove(currentEvent);
    }
}
