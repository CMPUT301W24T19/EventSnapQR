package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.pressBack;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.hamcrest.CoreMatchers.anything;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
@LargeTest

/**
 *  US 02.08.01 As an attendee, I want to browse event posters/event details of other events.
 *  US 02.04.01 As an attendee, I want to view event details and announcements within the app.
 */

public class BrowseEventsTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void browseEvents_and_ViewDetails() throws InterruptedException {
        onView(withId(R.id.browse_events_button)).perform(click());
        Thread.sleep(2000); // Not recommended for real tests
        onData(anything()).inAdapterView(withId(R.id.events)).atPosition(0).perform(click());
        Thread.sleep(2000);
        pressBack();
        Thread.sleep(2000);
        onView(withId(R.id.events)).check(matches(isDisplayed()));
    }
}
