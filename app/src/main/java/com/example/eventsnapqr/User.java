package com.example.eventsnapqr;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a user object that can take the roles of an
 * attendee, organizer and/or an admin. The user has a name,
 * homepage, contact info, and a set of roles.
 */
public class User {
    private String name; // name of the user
    private String homepage; // user website
    private String contactInfo; // further contact information
    private List<Role> userRoles; // list of roles this user currently has
    private FirebaseController firebaseController;

    public User() {}
    /**
     * constructor for a user using only their name
     * @param name name of the user
     */
    public User(String name) {
        this.name = name;
        firebaseController = FirebaseController.getInstance();
        firebaseController.addUser(this);
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
     * getter method for roles of the user
     * @return list of roles
     */
    public List<Role> getRoles() {
        return userRoles;
    }
}
