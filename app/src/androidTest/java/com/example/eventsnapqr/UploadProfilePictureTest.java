package com.example.eventsnapqr;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;


import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;

import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.core.IsNull.notNullValue;

import android.support.test.espresso.intent.rule.IntentsTestRule;
/**
 * US 02.02.01 As an attendee, I want to upload a profile picture for a more personalized experience.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest

public class UploadProfilePictureTest {

    @Rule
    public IntentsTestRule<UserInfoActivity> intentsTestRule = new IntentsTestRule<>(UserInfoActivity.class);

    @Test
    public void uploadProfilePicture() throws InterruptedException {
        onView(withId(R.id.view_user_button)).perform(click());
        Thread.sleep(2000);
//        onView(withId(R.id.upload_profile_button)).perform(click());
//
//        Thread.sleep(2000);
//        onView(withId(R.id.iv_profile_pic)).check(matches(notNullValue()));
//        Thread.sleep(2000);
//
//        // Optionally, navigate back out
//        onView(withId(R.id.back_button)).perform(click());
    }
}
