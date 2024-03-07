package com.example.eventsnapqr;

import java.util.List;

public interface Organizer {

    // US 01.01.01
    Event createNewEvent(String eventName, String eventDescription);

    // US 01.01.02
    void reuseQRCodeForEvent(Event event, String qrCode);

    void reuseQRCodeForEvent(Event event, QR qrCode);

    // US 01.02.01

    void viewEventAttendees(String eventId);

    <OnAttendeesRetrievedListener> void viewEventAttendees(String eventId, OnAttendeesRetrievedListener listener);

    void viewEventAttendees(String eventId, User.AttendeesCallback callback);

    // US 01.03.01
    void sendNotificationsToAttendees(String eventId, String message);

    // US 01.04.01
    void uploadEventPoster(String eventId, String posterUri);

    // US 01.05.01
    void trackRealTimeAttendance(String eventId);

    // US 01.06.01
    void shareQRCodeImage(String qrCode, String platform);

    // US 01.07.01
    void generatePromotionQRCode(String eventId);

    // US 01.08.01
    void viewCheckInLocations(String eventId);

    // US 01.09.01
    int getCheckInCount(String userId, String eventId);

    // US 01.10.01 [New for Part 3]
    List<User> viewSignedUpAttendees(String eventId);

    // US 01.11.01 [New for Part 3]
    void limitNumberOfAttendees(String eventId, int limit);
}
