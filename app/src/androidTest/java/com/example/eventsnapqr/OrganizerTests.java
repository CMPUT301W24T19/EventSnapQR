package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.Matchers.anything;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

import java.util.Date;

@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.JVM)
public class OrganizerTests {
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
    @After
    public void cleanUp(){

    }
    @Test
    public void notificationTest() throws InterruptedException {

        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), OrganizeAnEventActivity.class);
        ActivityScenario<OrganizeAnEventActivity> scenarioOne = ActivityScenario.launch(intent);
        Thread.sleep(5000);
        FirebaseController fbc = FirebaseController.getInstance();
        String eventId = fbc.getUniqueEventID();
        Event newEvent = new Event(new User(androidId), "testEvent", "testEventDescription", null, 5, eventId, new Date(), new Date(), "123 Spooner St.","QRLink");

        fbc.addEvent(newEvent);
        //onView(withId(R.id.editTextEventName)).perform(scrollTo(),click(), typeText("TestEvent"), closeSoftKeyboard());
        //onView(withId(R.id.editTextEndDate)).perform(scrollTo(),click(), typeText(""), closeSoftKeyboard());
        //onView(withId(R.id.editText)).perform(scrollTo(),click(), typeText("TestEvent"), closeSoftKeyboard());

        Thread.sleep(5000);
        Intent intentTwo = new Intent(ApplicationProvider.getApplicationContext(), ManageEventActivity.class);
        ActivityScenario<ManageEventActivity> scenarioTwo = ActivityScenario.launch(intentTwo);
        Thread.sleep(5000);
        onView(withText("Organized")).perform(click());
        Thread.sleep(5000);

        onData(anything())
                .inAdapterView(Matchers.allOf(withId(R.id.events), isDisplayed()))
                .atPosition(0)
                .onChildView(withId(R.id.eventName))
                .check(matches(withText("Test Event")))
                .perform(click());
    }

}