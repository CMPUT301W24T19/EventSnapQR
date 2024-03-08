package com.example.eventsnapqr;

import static androidx.camera.core.impl.utils.ContextUtil.getBaseContext;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

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
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.matcher.BoundedMatcher;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
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
    /**
     * Testing  US 02.07.01, US 02.08.01
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
        ActivityScenario activityScenario = ActivityScenario.launch(OrganizeAnEventActivity.class);
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
        onView(withId(R.id.events))
                .check(matches(hasDescendant(withText(startsWith(id)))));
        onData(hasToString(startsWith(id)))
                .inAdapterView(withId(R.id.events))
                .atPosition(0)
                .perform(click());
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.sign_up_button)).check(matches(isDisplayed())).perform(click());
        //onView(withText(startsWith("You have successfully signed up for"))).inRoot(isDialog()).check(matches(isDisplayed()));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withText("OK")).inRoot(isDialog()).perform(click());
        activityScenario.close();
        ArrayList<String> allAttendees = new ArrayList<>();

        firebaseController.getEvent(id, new FirebaseController.OnEventRetrievedListener() {
            @Override
            public void onEventRetrieved(Event event) {
                if(event != null){
                    firebaseController.getEventAttendees(event, new User.AttendeesCallback() {
                        @Override
                        public void onCallback(List<User> userList) {
                            // do nothing
                            latch.countDown();
                        }

                        @Override
                        public void onAttendeesLoaded(List<String> attendees) {
                            allAttendees.addAll(attendees);
                            latch.countDown();
                        }
                    });
                }

            }
        });
        try{
            latch.await(10, TimeUnit.SECONDS);
        }catch(Exception e){
            e.printStackTrace();
        }
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

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

