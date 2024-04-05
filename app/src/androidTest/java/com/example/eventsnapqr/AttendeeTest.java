package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onData;
import com.google.firebase.firestore.CollectionReference;

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

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.widget.Toast;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.action.ViewActions;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.firestore.CollectionReference;
import com.google.zxing.client.android.Intents;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
/**
 *  Test class for testing Attendee
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AttendeeTest {
    private String id;
    Context context = InstrumentationRegistry.getInstrumentation().getContext();
    ContentResolver contentResolver = context.getContentResolver();
    String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

    /**
     * US 02.01.01 test (in progrss)
     */
    @Test
    public void QRScanTest(){
        String eventID = "testeventid";
        FirebaseController firebaseController = FirebaseController.getInstance();
        Event newEvent = new Event(new User(), "testEvent", "testEventDescription", null, 5, eventID, new Date(), new Date(), true);
        firebaseController.addEvent(newEvent);
        CountDownLatch latch = new CountDownLatch(1);
        int requestCode = 0; // the request code used when starting the scan
        int resultCode = Activity.RESULT_OK;
        Intent data = new Intent();
        String validQRCodeContents = "eventsnapqr/validEventId";

        IntentResult intentResultMock = Mockito.mock(IntentResult.class);

        Mockito.when(intentResultMock.getContents()).thenReturn(validQRCodeContents);

        Mockito.when(IntentIntegrator.parseActivityResult(
                Mockito.anyInt(), Mockito.anyInt(), Mockito.any())).thenReturn(intentResultMock);

        ScanQRActivity activityUnderTest = Mockito.mock(ScanQRActivity.class);
        Mockito.doCallRealMethod().when(activityUnderTest).onActivityResult(requestCode, resultCode, data);


        // Act
        activityUnderTest.onActivityResult(requestCode, resultCode, data);

        firebaseController.isAttendee(androidId, newEvent, new FirebaseController.AttendeeCheckCallback() {
            @Override
            public void onChecked(boolean isAttendee, Event event) {
                assertEquals(true, isAttendee);
                latch.countDown();
            }
        });
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        firebaseController.deleteEvent(newEvent, new FirebaseController.FirestoreOperationCallback() {
            @Override
            public void onCompleted() {

            }
        });
    }

    /**
     * Test for  US 02.07.01, US 02.04.01
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
        Event newEvent = new Event(new User(), "testEvent", "testEventDescription", null, 5, id, new Date(), new Date(), true);
        firebaseController.addEvent(newEvent);

        // create an intent and put the event ID as an extra

        String announcement = "Test Announcement";
        FirebaseController firebaseControllerTwo = FirebaseController.getInstance();
        //CollectionReference announcementsRef = firebaseController.collection("events").document(id).collection("announcements");
        Map<String, Object> announcementData = new HashMap<>();
        announcementData.put("message", announcement);
        announcementData.put("timestamp", new Date());
        CountDownLatch latch = new CountDownLatch(1);
        /*
        announcementsRef.add(announcementData)
                .addOnSuccessListener(documentReference -> latch.countDown())
                .addOnFailureListener(e -> latch.countDown());

         */
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), BrowseEventsActivity.class);
        intent.putExtra("eventID", id);
        // launch BrowseEventsActivity with the intent
        ActivityScenario<BrowseEventsActivity> activityScenario = ActivityScenario.launch(intent);

        // US 02.04.01 check
        onView(withId(R.id.editTextAnnouncements))
                .check(matches(withText(announcement)));
        onView(withId(R.id.sign_up_button)).perform(click());

        firebaseController.isAttendee(androidId, newEvent, new FirebaseController.AttendeeCheckCallback() {
            @Override
            public void onChecked(boolean isAttendee, Event event) {
                assertTrue(isAttendee);
                latch.countDown();
            }
        });

        // Enable animations after the test is finished
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 1");
    }
}

