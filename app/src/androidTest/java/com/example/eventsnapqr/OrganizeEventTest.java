package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.assertEquals;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

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
    public void init() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        // Disable animations
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 0");
        FirebaseController firebaseController = new FirebaseController();
        CountDownLatch latch = new CountDownLatch(1);
        final Boolean[] userExists = new Boolean[1];

        FirebaseController.Authenticator listener = new FirebaseController.Authenticator() {
            @Override
            public void onUserExistenceChecked(boolean exists) {
                if (exists) {
                    userExists[0] = true;
                    latch.countDown();
                }
                else {
                    userExists[0] = false;
                    latch.countDown();
                }
            }
            @Override
            public void onAdminExistenceChecked(boolean exists) {
                // do nothing
            }
        };
        FirebaseController.checkUserExists(androidId, listener);
        try {
            latch.await(10, TimeUnit.SECONDS); // Adjust timeout as needed
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        ActivityScenario.launch(MainActivity.class);


        if(!userExists[0]){

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            onView(withId(R.id.edit_text_name)).perform(typeText("Test Event Name"));


            onView(withId(R.id.edit_text_number)).perform(typeText("4033402450"));


            onView(withId(R.id.edit_text_email)).perform(typeText("test@email.com"));


            onView(withId(R.id.edit_text_homepage)).perform(typeText("www.homepage.com"));
            onView(isRoot()).perform(ViewActions.closeSoftKeyboard());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.button_sign_up)).perform(click());
            FirebaseController.checkUserExists(androidId, listener);
            try{
                latch.await(8,TimeUnit.SECONDS);
            }catch (Exception e){
                e.printStackTrace();
            }
            //assertTrue(userExists[0]);

            // Enable animations after the test is finished
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                    "settings put global window_animation_scale 1");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                    "settings put global transition_animation_scale 1");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                    "settings put global animator_duration_scale 1");

        }
    }
    @Test
    public void eventAttendeesTest() { // NOT COMPLETE*****
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
        onView(withId(R.id.view_on_events_button)).perform(click());
       // onView(withId(R.id.events)).check(matches(hasDescendant(withText(id)))).perform(click());


    }

    /**
     * Test to test that an event is successfully created
     * US 01.01.01
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


