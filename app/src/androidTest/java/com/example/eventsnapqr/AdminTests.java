package com.example.eventsnapqr;

//import org.junit.Rule;

import static android.app.PendingIntent.getActivity;
import static androidx.test.espresso.Espresso.closeSoftKeyboard;
import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static junit.framework.TestCase.assertEquals;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.view.inputmethod.InputMethodManager;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.action.CloseKeyboardAction;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

//import org.junit.Rule;
//@RunWith(AndroidJUnit4.class)
@LargeTest
@RunWith(AndroidJUnit4.class)
public class AdminTests {

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

    private Boolean userExists;

    /**
     * US 02.06.01 ****user must not have account for test to work****
     */

    @Test
    public void browseAndDeleteProfilesTest() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();
        String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        androidId.replaceAll("a","b");
        User user = new User(androidId);
        user.setName(androidId);
        FirebaseController firebaseController = new FirebaseController();
        firebaseController.addUser(user);
        // Disable animations
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global window_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global transition_animation_scale 0");
        InstrumentationRegistry.getInstrumentation().getUiAutomation().executeShellCommand(
                "settings put global animator_duration_scale 0");

        CountDownLatch latch = new CountDownLatch(1);
        final Boolean[] userExists = new Boolean[1];

        FirebaseController.Authenticator listener = new FirebaseController.Authenticator() {
            @Override
            public void onUserExistenceChecked(boolean exists) {
                if (exists) {
                    userExists[0] = true;
                    latch.countDown();
                } else {
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


        if (userExists[0]) {

            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            onView(withId(R.id.admin_button)).perform(click());
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            onView(withId(R.id.buttonBrowseUserProfiles));
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
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



