package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.anything;
import static org.junit.Assert.assertTrue;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.annotation.NonNull;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Date;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.JVM)
public class MoreAttendeeTests {
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
    @Before
    public void deleteEvents() throws InterruptedException {
        final Semaphore semaphore = new Semaphore(0);
        FirebaseController fbc = FirebaseController.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference eventsRef = db.collection("events");

        Query query = eventsRef.whereEqualTo("organizerID", androidId);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        eventsRef.document(document.getId()).delete()
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> deleteTask) {
                                        if (deleteTask.isSuccessful()) {} else {}
                                        semaphore.release();
                                    }
                                });
                    }
                } else {
                    semaphore.release();
                }

                if (task.getResult().size() == 0) {
                    semaphore.release();
                }
            }
        });
        semaphore.acquire();
    }


    /**
     * US 02.09.01, US 02.07.01 Test
     * As an attendee,
     * I want to know what events I signed up for currently and in the future.
     */
    @Test
    public void viewAttendingEventsTest() throws InterruptedException {
        FirebaseController fbc = FirebaseController.getInstance();
        String eventId = fbc.getUniqueEventID();
        User user = new User(androidId);
        fbc.addUser(user);
        Event newEvent = new Event(user, "testEvent", "testEventDescription", null, 5, eventId, new Date(), new Date(), "123 Spooner St.", true);
        fbc.addEvent(newEvent);
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), BrowseEventsActivity.class);
        intent.putExtra("eventID", eventId);
        ActivityScenario<BrowseEventsActivity> scenario = ActivityScenario.launch(intent);

        Thread.sleep(5000);
        // US 02.07.01
        onView(withText("Sign-Up")).perform(click());
        scenario.close();

        fbc.isAttendee(androidId, newEvent, new FirebaseController.AttendeeCheckCallback() {
            @Override
            public void onChecked(boolean isAttendee, Event event) {
                assertTrue(isAttendee); // US 02.07.01 check
            }
        });
        Intent intentTwo = new Intent(ApplicationProvider.getApplicationContext(), BrowseEventsActivity.class);
        ActivityScenario<BrowseEventsActivity> scenarioTwo = ActivityScenario.launch(intentTwo);

        Thread.sleep(5000);
        onView(withText("Attending")).perform(click());
        Thread.sleep(5000);
        onData(anything())
                .inAdapterView(Matchers.allOf(withId(R.id.events),
                isDisplayed())).atPosition(0)
                .onChildView(withId(R.id.eventName))
                .check(matches(withText("testEvent")));
        // now test notifications
        scenarioTwo.close();
        CountDownLatch latch = new CountDownLatch(1);


    }


}