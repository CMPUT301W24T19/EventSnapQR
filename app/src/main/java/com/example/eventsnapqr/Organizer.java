package com.example.eventsnapqr;

import java.util.List;

public class Organizer {
    private List<Event> myEvents;
    public Organizer(){}
    private void createEvent(QR qrCode){
        Event newEvent = new Event(this, qrCode);
        myEvents.add(newEvent);


    }
}
