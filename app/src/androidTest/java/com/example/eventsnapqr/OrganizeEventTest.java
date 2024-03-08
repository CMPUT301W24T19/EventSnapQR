package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.TestCase.assertEquals;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizeEventTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);
    @Before
    public void setUp() throws Exception {
        // Initialize necessary setup
    }
    @Test
    public void eventAttendeesTest() {
        FirebaseController firebaseController = new FirebaseController();
        String id = firebaseController.getUniqueEventID();

        // Launch OrganizeAnEventActivity and create the event
        ActivityScenario.launch(OrganizeAnEventActivity.class);
        onView(withId(R.id.editTextEventName)).perform(typeText(id));
        onView(withId(R.id.editTextEventDesc)).perform(typeText("Event Description"));
        onView(withId(R.id.button_create)).perform(click());
        CountDownLatch latch = new CountDownLatch(1);
        // Load events from Firebase
        ArrayList<Event> allEvents = new ArrayList<>();
        firebaseController.getAllEvents(new FirebaseController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(ArrayList<Event> events) {
                allEvents.addAll(events);
                // Signal that events are loaded
                latch.countDown();
            }
        });

        // Wait for events to load
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.browse_events_button)).perform(click());
        // Verify if the event is listed in the UI
        onView(withId(R.id.events)).check(matches(hasDescendant(withText(id)))).perform(click());

    }

    /**
     * Test to test that an event is successfully created
     */
    @Test
    public void organizeEventTest() {
        FirebaseController firebaseController = new FirebaseController();
        String id = firebaseController.getUniqueEventID();

        // Launch OrganizeAnEventActivity and create the event
        ActivityScenario.launch(OrganizeAnEventActivity.class);
        onView(withId(R.id.editTextEventName)).perform(typeText(id));
        onView(withId(R.id.editTextEventDesc)).perform(typeText("Event description"));

        onView(withId(R.id.button_create)).perform(click());
        // Use CountDownLatch to wait for Firebase operation to complete
        CountDownLatch latch = new CountDownLatch(1);
        firebaseController.getAllEvents(new FirebaseController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(ArrayList<Event> events) {
                // Verify the event after it's loaded
                for (Event event : events) {
                    if (event.getEventName().equals(id)) {
                        assertEquals(event.getEventName(), id);
                        latch.countDown(); // Signal that events are loaded
                        break;
                    }
                }
            }
        });

        try {
            latch.await(10, TimeUnit.SECONDS); // Adjust timeout as needed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}


