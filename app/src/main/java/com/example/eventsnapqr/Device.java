package com.example.eventsnapqr;
import android.content.Context;

/**
 * Represents a users device. Has a unique identifier for each device. Constructor takes in
 * application context. Users and devices are one-to-one. This allows whitelisting users/devices
 * using the firestore database
 */
public class Device {
    private String deviceID;
    private User associatedUser;
    private Boolean hasAdminPrivileges;
    private Boolean userInfoEntered;

    public Device(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public User getAssociatedUser() {
        return associatedUser;
    }

    public void setAssociatedUser(User associatedUser) {
        this.associatedUser = associatedUser;
    }

    public Boolean getHasAdminPrivileges() {
        return hasAdminPrivileges;
    }

    public void setHasAdminPrivileges(Boolean hasAdminPrivileges) {
        this.hasAdminPrivileges = hasAdminPrivileges;
    }

    public Boolean getUserInfoEntered() {
        return userInfoEntered;
    }

    public void setUserInfoEntered(Boolean userInfoEntered) {
        this.userInfoEntered = userInfoEntered;
    }
}
