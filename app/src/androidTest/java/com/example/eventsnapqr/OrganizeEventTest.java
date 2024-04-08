package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static junit.framework.TestCase.assertEquals;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.FixMethodOrder;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;

/**
 *  Test class for testing organize events
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@FixMethodOrder(MethodSorters.JVM)
public class OrganizeEventTest {
    @Rule
    public ActivityScenarioRule<OrganizeAnEventActivity> scenario = new
            ActivityScenarioRule<OrganizeAnEventActivity>(OrganizeAnEventActivity.class);

    @Test
    public void geolocationTest(){
        // Launch OrganizeAnEventActivity and create the event
        ActivityScenario.launch(OrganizeAnEventActivity.class);
        //onView(withId(R.id.editTextEventName)).perform(typeText(id));
        onView(withId(R.id.editTextDescription)).perform(typeText("Event description"));
    }


}


