package com.example.eventsnapqr;

/**
 * indicates that a class is capable of being assigned as a role
 */
public interface Role {
    User getUser(); // returns the user associated the this instance of role
}
