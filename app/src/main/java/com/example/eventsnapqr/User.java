package com.example.eventsnapqr;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user object that has the ability of an
 * organizer and attendee
 */
public class User{
//public class User implements Attendee, Organizer {
    private String name; // name of the user
    private String homepage; // user website
    private String contactInfo; // further contact information
    private String deviceID; // the device id associated with the user

    /**
     * Constructor for user using their name and a unique device id
     * @param name
     * @param deviceID
     */
    public User(String name, String deviceID) {
        this.name = name;
        this.deviceID = deviceID;
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
     * getting method for contact information
     * @return contact information
     */
    public String getContactInfo() {
        return contactInfo;
    }

    /**
     * setter method for contact information
     * @param contactInfo new contact information
     */
    public void setContactInfo(String contactInfo) {
        this.contactInfo = contactInfo;
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
