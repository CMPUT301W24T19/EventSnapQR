package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.action.ViewActions.clearText;
import com.google.firebase.firestore.CollectionReference;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withTagValue;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesPattern;
import static org.junit.Assert.assertNotNull;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.util.Log;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

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
    Event newEvent = new Event();

    @After
    public void deleteEvent() {
        FirebaseController firebaseController = new FirebaseController();
        /**
        firebaseController.deleteEvent(newEvent, new FirebaseController.FirestoreOperationCallback() {
            @Override
            public void onCompleted() {
                // Completion logic here
            }
        });
         **/
        // Enable animations after the test is finished
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 1");
    }

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

    /**
     * US 02.02.03 Test, US 02.05.01 Test
     */
    @Test
    public void updateContactInfoTest() {
        FirebaseIdlingResource idlingResource = new FirebaseIdlingResource();
        Espresso.registerIdlingResources(idlingResource);

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), UserInfoActivity.class);
        intent.putExtra("androidId", androidId);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userRef = db.collection("users").document(androidId);
        String testname = "TESTNAME";
        userRef.update("name", testname)
                .addOnSuccessListener(aVoid -> {
                    Log.d("Update", "DocumentSnapshot successfully updated!");
                })
                .addOnFailureListener(e -> {
                    Log.w("Update", "Error updating document", e);
                });


        ActivityScenario<UserInfoActivity> scenario = ActivityScenario.launch(intent);
        try {

            Thread.sleep(7000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        // US US 02.05.01 check
        //onView(withId(R.id.iv_profile_pic)).check(matches(withTagValue(is((Object) "TE"))));

        onView(withId(R.id.button_edit_profile_button)).perform(click());
        String testName = "TESTNAME";

        onView(withId(R.id.editTextUserName)).perform(scrollTo(), click(), clearText(),typeText(testName), closeSoftKeyboard());

        String testEmail = "testemail@email.com";
        onView(withId(R.id.editTextEmail)).perform(scrollTo(), click(), clearText(),typeText(testEmail), closeSoftKeyboard());

        String testHomepage = "www.TestHomePage.com";
        onView(withId(R.id.editTextHomepage)).perform(scrollTo(), click(), clearText(),typeText(testHomepage), closeSoftKeyboard());

        onView(withId(R.id.saveButton)).perform(scrollTo(), click());

        FirebaseController firebaseController = new FirebaseController();
        idlingResource.setIdleState(false);

        firebaseController.getUser(androidId, user -> {
            assertNotNull(user);
            assertEquals(testEmail, user.getEmail());
            assertEquals(testName, user.getName());
            assertEquals(testHomepage, user.getHomepage());

            idlingResource.setIdleState(true); // Set idle state to true after the operation is complete
        });

        Espresso.unregisterIdlingResources(idlingResource);
    }



    /**
     * US 02.01.01 test (in progrss)
     */
    /**
    @Test
    public void QRScanTest(){
        String eventID = FirebaseController.getInstance().getUniqueEventID();
        FirebaseController firebaseController = FirebaseController.getInstance();
        newEvent = new Event(new User(), "testEvent", "testEventDescription", null, 5, id, new Date(), new Date(), "123 Spooner St.",true);
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

    }
**/
    /**
     * Test for  US 02.04.01, US 02.08.01
     */
    @Test
    public void viewEventAnnouncementTest(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseController firebaseController = new FirebaseController();
        id = firebaseController.getUniqueEventID();
        Event newEvent = new Event(new User(androidId), "testEvent", "testEventDescription", null, 5, id, new Date(), new Date(), "123 Spooner St.","QRLink");
        firebaseController.addEvent(newEvent);
        String announcement = "Test Announcement";
        CollectionReference announcementsRef = db.collection("events").document(id).collection("announcements");
        Map<String, Object> announcementData = new HashMap<>();
        announcementData.put("message", announcement);
        announcementData.put("timestamp", new Date());
        CountDownLatch latch = new CountDownLatch(1);

        announcementsRef.add(announcementData)
                .addOnSuccessListener(documentReference -> latch.countDown())
                .addOnFailureListener(e -> latch.countDown());
        try{
            latch.await(10, TimeUnit.SECONDS);
        }catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        // create an intent and put the event ID as an extra

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), BrowseEventsActivity.class);

        intent.putExtra("eventID", id);
        // launch BrowseEventsActivity with the intent
        ActivityScenario<BrowseEventsActivity> activityScenario = ActivityScenario.launch(intent);
        try {
            Thread.sleep(5000); // Wait for 1 second
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        // US 2.04.01 check
        onView(withId(R.id.editTextAnnouncements)).perform(scrollTo())
                .check(matches(withText(CoreMatchers.containsString("Test Announcement"))));
        /**
        onView(withId(R.id.editTextAnnouncements))
        onView(withId(R.id.editTextEmail))
                .perform(scrollTo())
                .check(matches(withText("• Test Announcement\n")));
**/
        firebaseController.isAttendee(androidId, newEvent, new FirebaseController.AttendeeCheckCallback() {
            @Override
            public void onChecked(boolean isAttendee, Event event) {
                // US 02.07.01 check
                assertTrue(isAttendee);
                latch.countDown();
            }
        });
        firebaseController.deleteEvent(newEvent, new FirebaseController.FirestoreOperationCallback() {
            @Override
            public void onCompleted() {

            }
        });

    }

}

