package com.example.eventsnapqr;

import java.util.List;

public class Event {
    private Organizer organizer;
    private QR qrCode;
    // private EventPoster poster;
    private List<?> attendees;
    public Event(Organizer organizer,QR qrCode){
        this.qrCode = qrCode;
        this.organizer = organizer;
    }


    /**
    private void setPoster(EventPoster poster){
        this.poster = poster;
    }
    **/
}
