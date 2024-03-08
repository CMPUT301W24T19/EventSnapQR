package com.example.eventsnapqr;

import static androidx.camera.core.impl.utils.ContextUtil.getBaseContext;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static junit.framework.TestCase.assertEquals;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AttendeeTest {
    private String id;
    /**
     * Testing  US 02.07.01
     */
    @Test
    public void signUpForEventTest(){
        // Disable animations
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 0");
        FirebaseController firebaseController = new FirebaseController();
        id = firebaseController.getUniqueEventID();

        // Launch OrganizeAnEventActivity and create the event
        ActivityScenario.launch(OrganizeAnEventActivity.class);
        onView(withId(R.id.editTextEventName)).perform(typeText(id));
        onView(withId(R.id.editTextEventDesc)).perform(typeText("Event description"));
        onView(withId(R.id.button_create)).perform(click());

        // Use CountDownLatch to wait for Firebase operation to complete
        CountDownLatch latch = new CountDownLatch(1);

        // Launch MainActivity and browse events
        ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.browse_events_button)).perform(click());
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onData(hasToString(startsWith(id)))
                .inAdapterView(withId(R.id.events)).atPosition(0)
                .perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.sign_up_button)).check(matches(isDisplayed())).perform(click());
        ArrayList<String> allAttendees = new ArrayList<>();

        firebaseController.getEvent(id, new FirebaseController.OnEventRetrievedListener() {
            @Override
            public void onEventRetrieved(Event event) {
                firebaseController.getEventAttendees(event, new User.AttendeesCallback() {
                    @Override
                    public void onCallback(List<User> userList) {
                        // do nothing
                    }

                    @Override
                    public void onAttendeesLoaded(List<String> attendees) {
                        allAttendees.addAll(attendees);
                    }
                });
            }
        });
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        while(allAttendees.size()==0){}
        int counter = 0;
        for(String attendeeId: allAttendees){
            if(androidId.equals(attendeeId)){
                assertEquals(attendeeId, androidId);
            }
        }

        // Enable animations after the test is finished
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 1");

    }

}

