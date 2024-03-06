package com.example.eventsnapqr;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user object that has the ability of an
 * organizer and attendee
 */
public class User {
//public class User implements Attendee, Organizer {
    private String name; // name of the user
    private String homepage; // users website
    private String phoneNumber; // users phone number
    private String email; // users email
    private String deviceID; // the device id associated with the user
    private List<Event> organizedEvents;
    private List<Event> attendingEvents;

    /**
     * Constructor for user using their name and a unique device id
     * @param name
     * @param deviceID
     */
    public User(String name, String deviceID) {
        this.name = name;
        this.deviceID = deviceID;
    }
    public User(String deviceID) {
        this.deviceID = deviceID;
    }
    /**
     * Constructor for user using their name, a unique device id, homepage and contact info
     * @param name name of the user
     * @param deviceID unique id of the users device
     * @param homepage the user can add a homepage if wanted
     * @param phoneNumber the users phone number (pt1 of contact info)
     * @param email the users email (pt2 of contact info)
     */
    public User(String name, String deviceID, String homepage, String phoneNumber, String email) {
        this.name = name;
        this.deviceID = deviceID;
        this.homepage = homepage;
        this.phoneNumber = phoneNumber;
        this.email = email;
    }

    /**
     * getter method for user name
     * @return name of the user
     */
    public String getName() {
        return name;
    }

    /**
     * setter method for name of the user
     * @param name name of the user
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * getter method for homepage
     * @return homepage of the user
     */
    public String getHomepage() {
        return homepage;
    }

    /**
     * setter method for homepage
     * @param homepage desired hompage
     */
    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    /**
     * getter method to retrieve users phone number
     * @return phone number of the user
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * setter method if the user needs to update their number
     * @param phoneNumber new phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * getter method to retrieve users email
     * @return users email
     */
    public String getEmail() {
        return email;
    }

    /**
     * setter method if the user needs to update their email
     * @param email new email
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * getter method for associated deviceID of the user
     * @return deviceID
     */
    public String getDeviceID() {
        return deviceID;
    }

    public User getUser() {

        return null;
    }
}
