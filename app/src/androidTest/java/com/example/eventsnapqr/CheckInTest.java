package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class CheckInTest {
    User user;
    User testUser;
    Event event;
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Rule
    public GrantPermissionRule mRuntimePermissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);

    @Before
    public void signUp() {
        CountDownLatch latch = new CountDownLatch(1);
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        user = new User(androidId, androidId, null, null, null);
        Log.d("TAG", "current user");
        FirebaseController.getInstance().deleteUser(user);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("TAG", "deleting user");
        FirebaseController.getInstance().addUser(user);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("TAG", "adding user");
        testUser = new User("testUser", "testUser", null, null, null);
        FirebaseController.getInstance().addUser(testUser);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        String eventID = FirebaseController.getInstance().getUniqueEventID();
        event = new Event(user, eventID, "test", null, null, eventID, new Date(), new Date(), "testAddress", true);
        Log.d("TAG", "Event ID: " + eventID);
        FirebaseController.getInstance().addEvent(event);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    @After
    public void after() {
        CountDownLatch latch = new CountDownLatch(1);
        Log.d("TAG", testUser.getDeviceID());
        FirebaseController.getInstance().removeAttendee(event.getEventID(), testUser.getDeviceID(), null);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseController.getInstance().deleteUser(testUser);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseController.getInstance().deleteEvent(event, new FirebaseController.FirestoreOperationCallback() {
            @Override
            public void onCompleted() {
                FirebaseFirestore.getInstance().collection("events").document(event.getEventID()).delete();
            }
        });
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseController.getInstance().deleteUser(user);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    @Test
    public void checkInTest() {
        // checking in user
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseController.getInstance().addOrganizedEvent(user, event);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseController.getInstance().addPromiseToGo(testUser, event);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseController.getInstance().addAttendeeToEvent(event, testUser);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        HashMap<String, Object> data = new HashMap<>();
        data.put("checkedIn", 1);
        data.put("latitude", "70");
        data.put("longitude", "70");
        FirebaseFirestore.getInstance().collection("events").document(event.getEventID()).collection("attendees").document(testUser.getDeviceID()).set(data);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.browse_events_button)).perform(click());
        onView(withText("Organized")).perform(click());
        try {
            latch.await(1, TimeUnit.SECONDS);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onData(anything()).inAdapterView(allOf(withId(R.id.events), isDisplayed())).atPosition(0).perform(click());
        try {
            latch.await(1, TimeUnit.SECONDS);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.filter_switch)).perform(click());
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(0).onChildView(withId(R.id.attendee_name)).check(matches(withText(testUser.getName())));
        onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(0).onChildView(withId(R.id.checkedIn_image)).check(matches(isDisplayed()));
    }
}
