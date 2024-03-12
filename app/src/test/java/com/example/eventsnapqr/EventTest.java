package com.example.eventsnapqr;

import static junit.framework.TestCase.assertFalse;
import static junit.framework.TestCase.assertTrue;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.Test;
import org.junit.runner.RunWith;
/**
 *  Test class for testing Events
 */
public class EventTest {

    private Event mockEvent() {
        User user = new User("1vf5d6f");
        Event event = new Event(user, "Good Event", "Its going to be a good event", "www.goodevent.com", 5, "123456","This event will begin January 1");
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
        String announcement = "Test Announcement";

        Event event = new Event(organizer, eventName, description, posterUri, maxAttendees, eventID, announcement);

        assertEquals(organizer, event.getOrganizer());
        assertEquals(eventName, event.getEventName());
        assertEquals(description, event.getDescription());
        assertEquals(posterUri, event.getPosterURI());
        assertEquals(maxAttendees, event.getMaxAttendees());
        assertEquals(eventID, event.getEventID());
        assertEquals(announcement, event.getAnnouncement());
    }
    @Test
    public void setAnnouncement() {
        Event event = mockEvent();
        String oldAnnouncement = event.getAnnouncement();
        event.setAnnouncement("This event will start Feb 1");
        assertFalse(oldAnnouncement.equals(event.getAnnouncement()));
    }
    @Test
    public void getAnnouncement() {
        Event event = mockEvent();
        assertEquals(event.getAnnouncement(), "This event will begin January 1");
    }
    @Test
    public void setPosterUri(String posterUri){
        Event event = mockEvent();
        String old = event.getPosterURI();
        event.setPosterURI("wwww.badevent.com");
        assertFalse(event.getPosterURI().equals(old));
    }
    @Test
    public void getPosterUri(String posterUri){
        Event event = mockEvent();
        assertEquals(event.getPosterURI(),"www.goodevent.com");
    }
    @Test
    public void setDescription(){
        Event event = mockEvent();
        String old = event.getDescription();
        event.setDescription("New description");
        assertFalse(event.getDescription().equals(old));
    }
    @Test
    public void getDescription(){
        Event event = mockEvent();
        assertEquals(event.getDescription(), "Its going to be a good event");
    }
    @Test
    public void getEventId(){


        Event event = mockEvent();
        assertEquals(event.getDescription(), "123456");
    }
    @Test
    public void setEventId(){
        Event event = mockEvent();
        String old = event.getEventID();
        event.setEventID("New EventId");
        assertFalse(event.getDescription().equals(old));

    }
    @Test
    public void getOrganizer(){
        Event event = mockEvent();
        assertEquals(event.getOrganizer().getDeviceID(), "1vf5d6f");
    }
    @Test
    public void setOrganizer(){
        Event event = mockEvent();
        String old = event.getOrganizer().getDeviceID();
        User organizer = new User("g37rhyf73");
        event.setOrganizer(organizer);
        assertFalse(event.getOrganizer().getDeviceID().equals(old));
    }
    @Test
    public void getEventName(){
        Event event = mockEvent();
        assertEquals(event.getEventName(), "Good Event");
    }
    @Test
    public void setEventName(){
        Event event = mockEvent();
        String old = event.getEventName();
        event.setEventName("New name");
        assertFalse(event.getEventName().equals(old));
    }

}
