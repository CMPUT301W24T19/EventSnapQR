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
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
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

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        /*
        FirebaseFirestore instance = FirebaseFirestore.getInstance();

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


        if(!userExists[0]){  // user does not exist

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }


            // signing up for event
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
        firebaseFirestore.collection("users").document(androidId).delete();
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
        firebaseFirestore.collection("users").document(androidId).set(testUser);
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
        onView(withText("Browse Profiles")).check(matches(isDisplayed()));
        onView(withId(R.id.button_back_button)).check(matches(isDisplayed()));
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
        onView(withId(R.id.buttonBrowseImages)).perform(click());
        onView(withId(R.id.adminMainPage)).check(doesNotExist());
        onView(withId(R.id.button_back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.user_profile_pictures)).check(matches(isDisplayed()));
        onView(withText(testUser.getName())).check(matches(isDisplayed()));
        onView(withId(R.id.user_profile_pictures))
                .perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
        onView(withText("Delete")).perform(click());
        onView(withText("Delete User")).check(matches(isDisplayed()));
        onView(withText("Are you sure you want to delete this user?")).check(matches(isDisplayed()));
        onView(withText("Yes")).check(matches(isDisplayed()));
        onView(withText("No")).check(matches(isDisplayed()));
        onView(withText("Yes")).perform(click());
        onView(withText(testUser.getName())).check(matches(not(isDisplayed())));



        // Enable animations after the test is finished
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 1");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 1");

    }
}

