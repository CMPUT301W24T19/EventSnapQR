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

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizeEventTest {
    @Rule
    public ActivityScenarioRule<MainActivity> scenario = new
            ActivityScenarioRule<MainActivity>(MainActivity.class);
    @Before
    public void setUp() throws Exception {
        // Initialize necessary setup
    }

    @Test
    public void organizeEventTest() {
        FirebaseController firebaseController = new FirebaseController();
        String id = firebaseController.getUniqueEventID();
        ActivityScenario.launch(OrganizeAnEventActivity.class);
        onView(withId(R.id.editTextEventName)).perform(typeText(id));
        onView(withId(R.id.editTextEventDesc)).perform(typeText("Event Description"));
        onView(withId(R.id.button_create)).perform(click());
        ArrayList<Event> allEvents = new ArrayList<>();
        // get event
        firebaseController.getAllEvents(new FirebaseController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(ArrayList<Event> events) {
                allEvents.addAll(events);
            }
        });
        while(allEvents.size()==0){} // wait for events to load
        for(Event event: allEvents){
            if(event.getEventName().equals(id)){
                assertEquals(event.getEventName(), id);
            }
        }

    }


}


