package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertEquals;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.JVM)
public class OrganizerTests {
    Context context = InstrumentationRegistry.getInstrumentation().getContext();
    ContentResolver contentResolver = context.getContentResolver();
    String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
    @Before
    public void beforeTest(){
        // Disable animations
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 0");
    }
    Event newEvent;
    User organizer = new User(androidId);
    Event retrievedEvent;
    /**
     * US 01.11.01 Test
     */
    @Test
    public void limitAttendeesTest() throws InterruptedException {

        FirebaseController fbc = FirebaseController.getInstance();
        String eventId = fbc.getUniqueEventID();
        int maxAttendees = 1;
        newEvent = new Event(organizer, "EventName", "EventDesc", null, maxAttendees,eventId, new Date(), new Date(), "45 Test Address", "QRLink");
        fbc.addEvent(newEvent);
        Thread.sleep(5000);
        CountDownLatch latch = new CountDownLatch(1);

        fbc.getEvent(eventId, new FirebaseController.OnEventRetrievedListener() {
            @Override
            public void onEventRetrieved(Event event) {
                retrievedEvent = event;
                latch.countDown();
            }
        });
        latch.await(5, TimeUnit.SECONDS);

        assertEquals((int)retrievedEvent.getMaxAttendees(), maxAttendees);





    }
    @After
    public void deleteEvent(){
        FirebaseController fbc = FirebaseController.getInstance();
        fbc.deleteEvent(newEvent, new FirebaseController.FirestoreOperationCallback() {
            @Override
            public void onCompleted() {

            }
        });
    }
}