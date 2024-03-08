package com.example.eventsnapqr;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {
    private User user;

    @Before
    public void setUp() {
        user = new User("John Doe", "device123");
    }

    @Test
    public void testUserConstructor() {
        assertEquals("John Doe", user.getName());
        assertEquals("device123", user.getDeviceID());
    }

    @Test
    public void testNameGetterSetter() {
        user.setName("Jane Doe");
        assertEquals("Jane Doe", user.getName());
    }

    @Test
    public void testDeviceIDGetter() {
        assertEquals("device123", user.getDeviceID());
    }

    @Test
    public void testHomepageGetterSetter() {
        user.setHomepage("http://example.com");
        assertEquals("http://example.com", user.getHomepage());
    }

    @Test
    public void testPhoneNumberGetterSetter() {
        user.setPhoneNumber("1234567890");
        assertEquals("1234567890", user.getPhoneNumber());
    }

    @Test
    public void testEmailGetterSetter() {
        user.setEmail("john@example.com");
        assertEquals("john@example.com", user.getEmail());
    }

    @Test
    public void testProfilePictureGetterSetter() {
        user.setProfilePicture("profilePicUri");
        assertEquals("profilePicUri", user.getProfilePicture());
    }

    @Test
    public void testUserConstructorWithAllParams() {
        User newUser = new User("Jane Doe", "device456", "http://janedoe.com", "0987654321", "jane@doe.com");
        assertEquals("Jane Doe", newUser.getName());
        assertEquals("device456", newUser.getDeviceID());
        assertEquals("http://janedoe.com", newUser.getHomepage());
        assertEquals("0987654321", newUser.getPhoneNumber());
        assertEquals("jane@doe.com", newUser.getEmail());
    }
}
