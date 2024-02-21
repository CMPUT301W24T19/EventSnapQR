package com.example.eventsnapqr;

import java.util.List;

public class Event {
    private Organizer organizer;
    private QR qrCode;
    private List<?> attendees;
    public Event(Organizer organizer,QR qrCode){
        this.qrCode = qrCode;
        this.organizer = organizer;
    }

}
