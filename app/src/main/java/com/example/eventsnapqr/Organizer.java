package com.example.eventsnapqr;

import java.util.List;

public interface Organizer {

    // US 01.01.01
    Event createNewEvent(String eventName, String eventDescription);

    void viewEventAttendees(String eventId);

    <OnAttendeesRetrievedListener> void viewEventAttendees(String eventId, OnAttendeesRetrievedListener listener);

    void viewEventAttendees(String eventId, User.AttendeesCallback callback);

    // US 01.03.01
    void sendNotificationsToAttendees(String eventId, String message);

    // US 01.05.01
    void trackRealTimeAttendance(String eventId);

    // US 01.09.01
    int getCheckInCount(String userId, String eventId);

    // US 01.10.01 [New for Part 3]
    List<User> viewSignedUpAttendees(String eventId);
}
