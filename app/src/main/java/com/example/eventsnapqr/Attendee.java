package com.example.eventsnapqr;

import java.util.List;

/**
 * interface for user to implement the abilities of an attendee
 */
public interface Attendee {
    void checkIntoEvent(String qrCode);
    void uploadProfilePicture(String profilePictureUri);
    void removeProfilePicture();
    void updateProfileInfo(String name, String homepage, String phoneNumber, String email);
    void receivePushNotifications(String message); // Simplified version for demonstration
    Event viewEventDetails(String eventId);
    String generateProfilePictureFromName(String name);
    void signUpForEvent(Event event);
    List<Event> browseEvents();
    List<Event> viewSignedUpEvents();
}