package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static androidx.test.espresso.contrib.PickerActions.setDate;
import static androidx.test.espresso.contrib.PickerActions.setTime;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo; // Make sure it's imported from Hamcrest


import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.contrib.PickerActions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import android.content.Intent;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TimePicker;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.mockito.MockitoAnnotations;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 *  Test class for testing organize events
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.JVM)
public class OrganizeEventTest {


    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseController firebaseController;
    private String eventId;
    private Event newEvent;
    private String testEventName = "Test Event";
    private String testEventDesc = "This is a test event description.";
    private String maxAttendees = "10";
    private String testAddress = "144098 234 ave";
    private String startDate = "7/4/2024";
    private String endDate = "20/4/2024";
    private String startTime = "17:43";
    private String endTime = "17:43";



    public void setUp() {
        MockitoAnnotations.initMocks(this);

        db = mock(FirebaseFirestore.class);
        firebaseController = new FirebaseController();


        eventId = "mockEventId";
        newEvent = new Event(new User("mockOrganizerId"), "Mock Event", "This is a mock event.", null, 100, eventId, new Date(), new Date(), "123 Mock St.", "QRLink");

    }

    //

//        onView(withId(R.id.editTextEventName)).perform(scrollTo(), click(), clearText(),typeText(testEventName), closeSoftKeyboard());
//        onView(withId(R.id.editTextDescription)).perform(scrollTo(), typeText(testEventDesc), closeSoftKeyboard());
//        onView(withId(R.id.editTextMaxAttendees)).perform(scrollTo(), typeText(maxAttendees), closeSoftKeyboard());
//        onView(withId(R.id.editTextAddress)).perform(scrollTo(), typeText(testAddress), closeSoftKeyboard());
//        onView(withId(R.id.extendedFabCreateEvent)).perform(scrollTo(), click());

    /**
     * US 01.01.01 and US 01.11.01 Test
     * @throws InterruptedException
     */

    @Test
    public void testCreateAndVerifyEvent() throws InterruptedException {
        // Initialize FirebaseController and Firestore
        FirebaseController firebaseController = FirebaseController.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        User organizer = new User("organizerDeviceID", "Organizer Name", null, null, null);
        String eventName = "Test Event";
        String eventDescription = "This is a test event.";
        String posterUri = "http://example.com/poster.png";
        Integer maxAttendees = 100;
        Date startDateTime = new Date();
        Date endDateTime = new Date(startDateTime.getTime() + 3600000);
        String address = "123 Test St.";


        // Create an event object

        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean eventExists = new AtomicBoolean(false);

        eventId = firebaseController.getUniqueEventID();
        Event event = new Event(organizer, eventName, eventDescription, posterUri, maxAttendees, eventId, startDateTime, endDateTime, address, "TestQR");


        firebaseController.addEvent(event);
        Integer expectedMaxAttendees = maxAttendees;
        db.collection("events").document(eventId).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot document = task.getResult();
                if (document.exists()) {
                    Integer maximumAttendees = document.getLong("maxAttendees").intValue();
                    // US 01.11.01 check
                    assertEquals(expectedMaxAttendees, maximumAttendees);// US 01.11.01 check
                    eventExists.set(true);

                }
            }
            latch.countDown(); // Decrement latch count to resume the test thread
        });

        // Wait for async operations to complete
        latch.await(10, TimeUnit.SECONDS); // Adjust the timeout as necessary
        // Assert event was created successfully
        assertTrue("Event was not created successfully", eventExists.get());
        // test notifications
        Intent intentThree = new Intent(ApplicationProvider.getApplicationContext(), ManageEventActivity.class);
        ActivityScenario<ManageEventActivity> scenarioThree = ActivityScenario.launch(intentThree);
        Thread.sleep(5000);
        onView(withText("Organized")).perform(click());
        Thread.sleep(5000);

        onData(anything())
                .inAdapterView(Matchers.allOf(withId(R.id.events), isDisplayed()))
                .atPosition(0)
                .onChildView(withId(R.id.eventName))
                .check(matches(withText("Test Event")))
                .perform(click());

    }



    /**
     * US 01.01.02
     * @throws InterruptedException
     */
    @Test
    public void testReuseQRCodeForCheckIn() throws InterruptedException {
        FirebaseController firebaseController = FirebaseController.getInstance();

        Event mockEvent = createMockEvent();

        firebaseController.addEvent(mockEvent);

        String mockUserId = "mockUser123";
        firebaseController.addAttendeeToEvent(mockEvent, new User(mockUserId));
        CountDownLatch latch = new CountDownLatch(1);
        final AtomicBoolean isAttendeeAdded = new AtomicBoolean(false);

        firebaseController.checkUserInAttendees(mockEvent.getEventID(), mockUserId, new FirebaseController.OnUserInAttendeesListener() {
            @Override
            public void onUserInAttendees(boolean isInAttendees) {
                isAttendeeAdded.set(isInAttendees);
                latch.countDown();
            }

            @Override
            public void onCheckFailed(Exception e) {
                fail("Failed to check if user is an attendee: " + e.getMessage());
            }
        });

        latch.await(10, TimeUnit.SECONDS);
        assertTrue("User should have been added as an attendee", isAttendeeAdded.get());
    }


    /**
     * US 01.01.02
     * @throws InterruptedException
     */
    @Test
    public void testAttendeeCheckin() throws InterruptedException {

        firebaseController.addEvent(newEvent);


        String mockAttendeeId = "mockAttendeeId";
        firebaseController.addAttendeeToEvent(newEvent, new User(mockAttendeeId));

        Thread.sleep(1000);

        final AtomicBoolean isAttendeeAdded = new AtomicBoolean(false);
        CountDownLatch latch = new CountDownLatch(1);

        firebaseController.checkUserInAttendees(eventId, mockAttendeeId, new FirebaseController.OnUserInAttendeesListener() {
            @Override
            public void onUserInAttendees(boolean isInAttendees) {
                isAttendeeAdded.set(isInAttendees);
                latch.countDown();
            }

            @Override
            public void onCheckFailed(Exception e) {
                fail("Failed to check if user is an attendee: " + e.getMessage());
            }
        });

        latch.await(10, TimeUnit.SECONDS);

        assertTrue("Attendee should have been added to the event", isAttendeeAdded.get());
    }

    private Event createMockEvent() {
        User organizer = new User("organizerId", "Organizer", null, null, null);
        return new Event(organizer, "Test Event", "This is a test event.", null, 100, "mockEvent123", new Date(), new Date(), "Test Location", "TestQR");
    }



    @After
    public void tearDown() {

    }
    @Rule
    public ActivityScenarioRule<OrganizeAnEventActivity> scenario = new
            ActivityScenarioRule<OrganizeAnEventActivity>(OrganizeAnEventActivity.class);


}


