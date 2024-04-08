package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.pressBack;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.not;

import android.app.UiAutomation;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CoordinatesProvider;
import androidx.test.espresso.action.GeneralClickAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Tap;
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
    User checkedInUser;
    User notCheckedInUser;
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

        // adding and deleting user
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        user = new User(androidId, androidId, null, null, null);
        FirebaseController.getInstance().deleteUser(user);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        FirebaseController.getInstance().addUser(user, new Runnable() {
            @Override
            public void run() {

            }
        });

        FirebaseController.getInstance().addUser(user, null);

        Log.d("TAG", "after addition");
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // adding checked in user
        checkedInUser = new User("checkedInUser", "checkedInUser", null, null, null);

        FirebaseController.getInstance().addUser(checkedInUser, new Runnable() {
            @Override
            public void run() {

            }
        });
        FirebaseController.getInstance().addUser(checkedInUser, null);

        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // adding not checked in user
        notCheckedInUser = new User("notCheckedInUser", "notCheckedInUser", null, null, null);

        FirebaseController.getInstance().addUser(notCheckedInUser, new Runnable() {
            @Override
            public void run() {

            }
        });

        FirebaseController.getInstance().addUser(notCheckedInUser, null);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // adding event
        String eventID = FirebaseController.getInstance().getUniqueEventID();
        event = new Event(user, eventID, "test", null, null, eventID, new Date(), new Date(), "testAddress", "testQRLink");
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

        FirebaseController.getInstance().removeAttendee(event.getEventID(), checkedInUser.getDeviceID(), null);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseController.getInstance().deleteUser(checkedInUser);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseController.getInstance().removeAttendee(event.getEventID(), notCheckedInUser.getDeviceID(), null);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseController.getInstance().deleteUser(notCheckedInUser);
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
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // make organizer
        FirebaseController.getInstance().addOrganizedEvent(user, event);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // signing up and checking in one user
        FirebaseController.getInstance().addPromiseToGo(checkedInUser, event);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseController.getInstance().addAttendeeToEvent(event, checkedInUser);
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
        FirebaseFirestore.getInstance().collection("events").document(event.getEventID()).collection("attendees").document(checkedInUser.getDeviceID()).set(data);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // signing up only for other user
        FirebaseController.getInstance().addPromiseToGo(notCheckedInUser, event);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        FirebaseController.getInstance().addAttendeeToEvent(event, notCheckedInUser);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        data.put("checkedIn", 0);
        data.put("latitude", "");
        data.put("longitude", "");
        FirebaseFirestore.getInstance().collection("events").document(event.getEventID()).collection("attendees").document(notCheckedInUser.getDeviceID()).set(data);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // actually performing the check*/
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
        onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(0).onChildView(withId(R.id.attendee_name)).check(matches(withText(checkedInUser.getName())));

        onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(1).onChildView(withId(R.id.attendee_name)).check(matches(withText(notCheckedInUser.getName())));
        onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(1).onChildView(withId(R.id.checkedIn_image)).check(matches(not(isDisplayed())));

        onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(0).perform(click());
        try {
            latch.await(1, TimeUnit.SECONDS);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(checkedInUser.getName() + " has checked-in 1 time."));
        //onView(withId(R.id.page_name)).perform(click());
        onView(withText(event.getEventName())).perform(click());

        onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(0).perform(click());
        try {
            latch.await(1, TimeUnit.SECONDS);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withText(notCheckedInUser.getName() + " has checked-in 0 times."));
        onView(withText("OK")).inRoot(isDialog()).check(matches(isDisplayed())).perform(pressBack());
        try {
            latch.await(1, TimeUnit.SECONDS);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.filter_switch)).perform(click());
        try {
            latch.await(1, TimeUnit.SECONDS);
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(0).onChildView(withId(R.id.attendee_name)).check(matches(withText(checkedInUser.getName())));
        onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(0).onChildView(withId(R.id.checkedIn_image)).check(matches(isDisplayed()));
        //onData(anything()).inAdapterView(withId(R.id.attendee_list)).atPosition(1).onChildView(withId(R.id.attendee_name)).check(doesNotExist());
        onView(withText("Total Attendees: 2")).check(matches(isDisplayed()));
        onView(withText("Total Checked-In: 1")).check(matches(isDisplayed()));
    }

    public static ViewAction clickXY(final int x, final int y){
        return new GeneralClickAction(
                Tap.SINGLE,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {

                        final int[] screenPos = new int[2];
                        view.getLocationOnScreen(screenPos);

                        final float screenX = screenPos[0] + x;
                        final float screenY = screenPos[1] + y;
                        float[] coordinates = {screenX, screenY};

                        return coordinates;
                    }
                },
                Press.FINGER);
    }
}
