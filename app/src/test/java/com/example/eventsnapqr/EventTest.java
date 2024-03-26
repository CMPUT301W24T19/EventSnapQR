package com.example.eventsnapqr;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test class for testing Events
 */
public class EventTest {

    private Event mockEvent() {
        User user = new User("1vf5d6f");
        Date startDate = new Date();
        Date endDate = new Date();
        Event event = new Event(user, "Good Event", "It's going to be a good event", "www.goodevent.com", 5, "123456", startDate, endDate, true);
        event.addAnnouncement("This event will begin January 1"); // Add an announcement
        return event;
    }




    @Test
    public void testEventConstructor() {
        User organizer = new User("organizerId", "Organizer Name");
        String eventName = "Test Event";
        String description = "Test Event Description";
        String posterUri = "https://example.com/poster.jpg";
        Integer maxAttendees = 100;
        String eventID = "eventId";

        Event event = new Event(organizer, eventName, description, posterUri, maxAttendees, eventID, new Date(), new Date(), true);

        assertEquals(organizer, event.getOrganizer());
        assertEquals(eventName, event.getEventName());
        assertEquals(description, event.getDescription());
        assertEquals(posterUri, event.getPosterURI());
        assertEquals(maxAttendees, event.getMaxAttendees());
        assertEquals(eventID, event.getEventID());
        assertTrue(event.getAnnouncements().isEmpty());
    }

    @Test
    public void addAnnouncement() {
        Event event = mockEvent();
        List<String> oldAnnouncements = new ArrayList<>(event.getAnnouncements()); // Create a copy
        event.addAnnouncement("This event will start Feb 1");
        assertFalse(oldAnnouncements.equals(event.getAnnouncements()));
    }

    @Test
    public void getAnnouncements() {
        Event event = mockEvent();
        assertEquals(event.getAnnouncements().get(0), "This event will begin January 1");
    }

    @Test
    public void setPosterUri() {
        Event event = mockEvent();
        String old = event.getPosterURI();
        event.setPosterURI("wwww.badevent.com");
        assertFalse(event.getPosterURI().equals(old));
    }

    @Test
    public void getPosterUri() {
        Event event = mockEvent();
        assertEquals(event.getPosterURI(), "www.goodevent.com");
    }

    @Test
    public void setDescription() {
        Event event = mockEvent();
        String old = event.getDescription();
        event.setDescription("New description");
        assertFalse(event.getDescription().equals(old));
    }

    @Test
    public void getDescription() {
        Event event = mockEvent();
        assertEquals(event.getDescription(), "It's going to be a good event");
    }

    @Test
    public void getEventId() {
        Event event = mockEvent();
        assertEquals(event.getEventID(), "123456");
    }

    @Test
    public void setEventId() {
        Event event = mockEvent();
        String old = event.getEventID();
        event.setEventID("New EventId");
        assertFalse(event.getEventID().equals(old));
    }

    @Test
    public void getOrganizer() {
        Event event = mockEvent();
        assertEquals(event.getOrganizer().getDeviceID(), "1vf5d6f");
    }

    @Test
    public void setOrganizer() {
        Event event = mockEvent();
        String old = event.getOrganizer().getDeviceID();
        User organizer = new User("g37rhyf73");
        event.setOrganizer(organizer);
        assertFalse(event.getOrganizer().getDeviceID().equals(old));
    }

    @Test
    public void getEventName() {
        Event event = mockEvent();
        assertEquals(event.getEventName(), "Good Event");
    }

    @Test
    public void setEventName() {
        Event event = mockEvent();
        String old = event.getEventName();
        event.setEventName("New name");
        assertFalse(event.getEventName().equals(old));
    }
}
