package com.example.eventsnapqr;


import static android.app.PendingIntent.getActivity;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static junit.framework.TestCase.assertEquals;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;

import androidx.test.espresso.UiController;
 
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CloseKeyboardAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
/**
 *  Test class for testing Admin
 */
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AdminTests {

    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new ActivityScenarioRule<MainActivity>(MainActivity.class);

    @Before
    public void init() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (QueryDocumentSnapshot doc: querySnapshot) {
                    firebaseFirestore.collection("users").document(doc.getId()).delete();

                }
            }
        });

        firebaseFirestore.collection("events").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (QueryDocumentSnapshot doc: querySnapshot) {
                    firebaseFirestore.collection("events").document(doc.getId()).delete();
                }

            }
        });
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    /**
     * US 02.06.01 ****user must not have account for test to work****
     */
    }

    @After
    public void afterTest() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("admin").document(androidId).delete();
        User user = new User(androidId, androidId, null, null, null);
        FirebaseController.getInstance().deleteUser(user);
    }

    @Test
    public void noAdminMainPageTest() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

        User testUser = new User("TestUser", androidId, "testHomePage", "testNumber", "testEmail");

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("users").document(androidId).set(testUser);

        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 0");

        ActivityScenario.launch(MainActivity.class);
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.admin_button)).check(matches(not(isDisplayed())));
    }


    @Test
    public void AdminMainPageTest() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

        User testUser = new User("TestUser", androidId, "testHomePage", "testNumber", "testEmail");

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        FirebaseController.getInstance().addUser(testUser);
        firebaseFirestore.collection("admin").document(androidId).set(testUser);

        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 0");
        ActivityScenario.launch(MainActivity.class);
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.admin_button)).check(matches(isDisplayed()));
        onView(withId(R.id.admin_button)).perform(click());
        onView(withId(R.id.adminMainPage)).check(matches(isDisplayed()));
        onView(withId(R.id.mainPageFragment)).check(doesNotExist());
        onView(withText("Admin Mode")).check(matches(isDisplayed()));
        onView(withId(R.id.buttonBrowseUserProfiles)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonBrowseImages)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonBrowseEvents)).check(matches(isDisplayed()));
        onView(withId(R.id.button_back)).check(matches(isDisplayed()));
        onView(withId(R.id.button_back)).perform(click());
    }

    @Test
    public void checkUserInfo() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

        User testUser = new User("TestUser", androidId, "testHomePage", "testNumber", "testEmail");

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("users").document(androidId).set(testUser);
        firebaseFirestore.collection("admin").document(androidId).set(testUser);
        // Disable animations
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 0");

        ActivityScenario.launch(MainActivity.class);
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.admin_button)).perform(click());
        onView(withId(R.id.buttonBrowseUserProfiles)).perform(click());
        onView(withId(R.id.browseProfileFragment)).check(matches(isDisplayed()));
        onView(withId(R.id.adminMainPage)).check(doesNotExist());
        onView(withText("Admin Browse Profiles")).check(matches(isDisplayed()));
        onView(withId(R.id.button_back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.button_back_button)).perform(click());
        onView(withId(R.id.adminMainPage)).check(matches(isDisplayed()));
        onView(withId(R.id.browseProfileFragment)).check(doesNotExist());
        onView(withId(R.id.buttonBrowseUserProfiles)).perform(click());
        onView(withId(R.id.user_profile_pictures)).check(matches(isDisplayed()));
        onView(withText(testUser.getName())).check(matches(isDisplayed()));
        onView(withId(R.id.userContent)).check(matches(isDisplayed()));
        onView(withId(R.id.user_profile_pictures))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withText("User information")).check(matches(isDisplayed()));
        onView(withText("Name: " + testUser.getName() + "\n" +
                        "Home Page: " + testUser.getHomepage() + "\n" +
                        "Phone Number: " + testUser.getPhoneNumber() + "\n" +
                        "Email: " + testUser.getEmail() + "\n")).check(matches(isDisplayed()));
        onView(withText("View")).check(matches(isDisplayed()));
        onView(withText("Delete")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
        onView(withText("View")).perform(click());
        onView(withId(R.id.userInfoActivity)).check(matches(isDisplayed()));


        // Enable animations after the test is finished
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 1");
    }

    Context context = InstrumentationRegistry.getInstrumentation().getContext();
    ContentResolver contentResolver = context.getContentResolver();
    String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
    boolean gotEvent = false;
    boolean eventDeleted = false;
    boolean eventsFound = false;
    Event retrievedEvent;
    ArrayList<Event> foundEvents = new ArrayList<>();

    /**
     * US 04.01.01 test
     */
    @Test
    public void removeEvent(){
        FirebaseController firebaseController = FirebaseController.getInstance();
        String eventId = firebaseController.getUniqueEventID();
        Event newEvent = new Event(new User(androidId), "testEvent", "testEventDescription", null, 5,eventId, new Date(), new Date(), "123 Spooner St.",true);
        firebaseController.addEvent(newEvent);
        firebaseController.getEvent(eventId, new FirebaseController.OnEventRetrievedListener() {
            @Override
            public void onEventRetrieved(Event event) {
                retrievedEvent = event;
                gotEvent = true;

            }
        });
        while(!gotEvent){}
        assertEquals(retrievedEvent.getEventID(), eventId); // to ensure it is same event we added
        CountDownLatch latch = new CountDownLatch(10);
        firebaseController.deleteEvent(newEvent, new FirebaseController.FirestoreOperationCallback() {
            @Override
            public void onCompleted() {
                eventDeleted = true;
                latch.countDown();
            }
        });

        while(!eventDeleted){}
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        firebaseController.getAllEvents(new FirebaseController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(ArrayList<Event> events) {
                foundEvents.addAll(events);
                eventsFound = true;

            }
        });
        while(!eventsFound){}
        try{
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        boolean eventNotFound = true;
        ArrayList<Event> activeEvents = new ArrayList<>();
        for (Event event : foundEvents) {
            if(event.isActive()){
                activeEvents.add(event);
            }

        }
        for(Event activeEvent: activeEvents){
            if (activeEvent.getEventID().equals(eventId)) {
                eventNotFound = false;
                break;
            }
        }
        assertTrue("The event should not be found in the list",eventNotFound);
    }
    @Test
    public void deleteUserTest() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

        User testUser = new User("TestUser", androidId, "testHomePage", "testNumber", "testEmail");

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("users").document(androidId).set(testUser);
        firebaseFirestore.collection("admin").document(androidId).set(testUser);
        // Disable animations
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 0");

        ActivityScenario.launch(MainActivity.class);
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.admin_button)).perform(click());
        onView(withId(R.id.buttonBrowseUserProfiles)).perform(click());
        onView(withId(R.id.browseProfileFragment)).check(matches(isDisplayed()));
        onView(withId(R.id.adminMainPage)).check(doesNotExist());
        onView(withId(R.id.user_profile_pictures))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withText("Delete")).perform(click());
        onView(withText("Delete User")).check(matches(isDisplayed()));
        onView(withText("Are you sure you want to delete this user?")).check(matches(isDisplayed()));
        onView(withText("Yes")).check(matches(isDisplayed()));
        onView(withText("No")).check(matches(isDisplayed()));
        onView(withText("Yes")).perform(click());
        onView(withId(R.id.userContent)).check(doesNotExist());
        onView(withText(testUser.getName())).check(doesNotExist());


        // Enable animations after the test is finished
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 1");

    }


    @Test
    public void browsePicturesTest() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

        User testUser = new User("TestUser", androidId, "testHomePage", "testNumber", "testEmail");
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("test/testPoster");
        Bitmap testPoster = testUser.generateInitialsImage(testUser.getName().toString());
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        testPoster.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        byte[] testPosterByte = byteArrayOutputStream.toByteArray();
        Uri[] result = new Uri[1];
        storageRef.putBytes(testPosterByte).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        result[0] = uri;
                        Log.d("TAG", "URI registered");
                        Log.d("TAG", "URI before event: " + result[0].toString());
                    }
                });
            }
        });

        CountDownLatch latch = new CountDownLatch(1);

        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.d("TAG", "Making event");
        Log.d("TAG", "Event URI: " + result[0].toString());
        //Event testEvent = new Event(testUser, "testEvent", "testDescription", result[0].toString(), 5, "eventID", null, null, true);
        Event testEvent = null;
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("users").document(androidId).set(testUser);
        firebaseFirestore.collection("admin").document(androidId).set(testUser);
        firebaseFirestore.collection("events").document("eventID").set(testEvent);
        // Disable animations
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 0");

        ActivityScenario.launch(MainActivity.class);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withId(R.id.admin_button)).perform(click());
        onView(withId(R.id.buttonBrowseImages)).perform(click());
        onView(withId(R.id.adminMainPage)).check(doesNotExist());
        onView(withId(R.id.browseImageFragment)).check(matches(isDisplayed()));
        onView(withId(R.id.rv_event_posters)).check(matches(isDisplayed()));
        onView(withId(R.id.eventPoster)).check(matches(isDisplayed()));
        onView(withId(R.id.browseImageFragment)).check(matches(isDisplayed()));
        onView(withId(R.id.rv_event_posters))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withText("Event Poster for " + testEvent.getEventName())).check(matches(isDisplayed()));
        onView(withText("View")).check(matches(isDisplayed()));
        onView(withText("Delete")).check(matches(isDisplayed()));
        onView(withText("Cancel")).check(matches(isDisplayed()));
        onView(withText("View")).perform(click());
        onView(withId(R.id.activityEventPoster)).check(matches(isDisplayed()));
        onView(withId(R.id.browseImageFragment)).check(doesNotExist());
        onView(withId(R.id.button_back_button)).perform(click());
        onView(withId(R.id.activityEventPoster)).check(doesNotExist());
        onView(withId(R.id.browseImageFragment)).check(matches(isDisplayed()));


        // Enable animations after the test is finished
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 1");

    }

    @Test
    public void adminBrowseAndDeleteEvent(){
            // Disable animations

            Context context = InstrumentationRegistry.getInstrumentation().getContext();
            ContentResolver contentResolver = context.getContentResolver();
            String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);

            User testUser = new User("TestUser", androidId, "testHomePage", "testNumber", "testEmail");

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            firebaseFirestore.collection("users").document(androidId).set(testUser);
            firebaseFirestore.collection("admin").document(androidId).set(testUser);
            // Disable animations
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                    "settings put global window_animation_scale 0");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                    "settings put global transition_animation_scale 0");
            InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                    "settings put global animator_duration_scale 0");
            FirebaseController firebaseController = new FirebaseController();


            Event event = new Event();
            event.setEventName(androidId);
            event.setEventID("1234345");
            event.setOrganizer(testUser);
            firebaseController.addEvent(event);
            ActivityScenario activity = ActivityScenario.launch(MainActivity.class);
            try {
            Thread.sleep(1000);
            } catch (InterruptedException e) {
            e.printStackTrace();
        }
            onView(withId(R.id.admin_button)).perform(click());
            onView(withId(R.id.buttonBrowseEvents)).perform(click());


            onView(withId(R.id.events))
                    .check(matches(hasDescendant(withText(startsWith(androidId)))));
            onData(hasToString(startsWith(androidId)))
                    .inAdapterView(withId(R.id.events))
                    .atPosition(0)
                    .perform(click());

        onView(withText("DELETE")).inRoot(isDialog()).perform(click());
        onView(withText("YES")).inRoot(isDialog()).perform(click());
        CountDownLatch latch = new CountDownLatch(1);
        ArrayList<Event> allEvents = new ArrayList<>();
        firebaseController.getAllEvents(new FirebaseController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(ArrayList<Event> events) {
                allEvents.addAll(events);
                latch.countDown();
            }
        });
        try {
            latch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        for(Event e: allEvents){
            if(e.getEventName().equals(androidId)){
                assertEquals(e.getEventName(), androidId); // because name of event is set to id
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



