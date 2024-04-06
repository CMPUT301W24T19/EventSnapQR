package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MassUserTest {
    private List<User> userList = new ArrayList<>();
    private List<Event> eventList = new ArrayList<>();
    private Random random = new Random();
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void init() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();

        CountDownLatch latch = new CountDownLatch(1);

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    FirebaseController.getInstance().getUser(doc.getId(), new FirebaseController.OnUserRetrievedListener() {
                        @Override
                        public void onUserRetrieved(User user) {
                            FirebaseController.getInstance().deleteUser(user);
                        }
                    });
                }
            }
        });
        Map<String, String> data = new HashMap<>();
        data.put("path", "events");

        firebaseFirestore.collection("events").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    FirebaseController.getInstance().getEvent(doc.getId(), new FirebaseController.OnEventRetrievedListener() {
                        @Override
                        public void onEventRetrieved(Event event) {
                            FirebaseController.getInstance().deleteEvent(event, new FirebaseController.FirestoreOperationCallback() {
                                @Override
                                public void onCompleted() {
                                    Log.d("TAG", "deleting event");
                                    firebaseFirestore.collection("events").document(doc.getId()).delete();
                                }
                            });
                        }
                    });
                }
            }
        });

        try {
            latch.await(60, TimeUnit.SECONDS);
            Thread.sleep(6000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        firebaseFirestore.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot querySnapshot) {
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    FirebaseController.getInstance().getUser(doc.getId(), new FirebaseController.OnUserRetrievedListener() {
                        @Override
                        public void onUserRetrieved(User user) {
                            FirebaseController.getInstance().deleteUser(user);
                        }
                    });
                }
            }
        });
        try {
            latch.await(60, TimeUnit.SECONDS);
            Thread.sleep(6000);
            latch.await(20, TimeUnit.SECONDS);
            Thread.sleep(20000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<User> userList = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 50; i++) {
            String userID = FirebaseController.getInstance().getUniqueEventID();
            User newUser = new User(userID, userID, null, null, null);
            userList.add(newUser);
            FirebaseController.getInstance().addUser(newUser);
        }

        for (int i = 0; i < 20; i++) {
            String eventID = FirebaseController.getInstance().getUniqueEventID();
            int randomEvent = random.nextInt(50);
            Event newEvent = new Event(userList.get(randomEvent), eventID, "Test Event Number: " + i, null, null, eventID, new Date(), new Date(999999999L), String.valueOf(i), true);
            eventList.add(newEvent);
            FirebaseController.getInstance().addEvent(newEvent);
        }

        try {
            latch.await(20, TimeUnit.SECONDS);
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void browseEventTest() {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        onView(withId(R.id.browse_events_button)).perform(click());
        //for (Event event: eventList) {
            //Log.d("TAG", event.getEventID());
        Log.d("TAG", "" + R.id.events);
        //onView(withText("Attending")).perform(click());
        onData(anything()).inAdapterView(withId(R.id.viewPager)).atPosition(0).atPosition(0).perform(click());
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //onView(allOf(withId(R.id.events), isDisplayingAtLeast(1))).check(matches(hasDescendant(withText("Hf1TIYBgqAIBqpnTW2P9"))));
            //onView(allOf(withId(R.id.events), isDisplayingAtLeast(1)))
             //       .perform(scrollTo(hasDescendant(withText("Hf1TIYBgqAIBqpnTW2P9"))));
            //onView(withText("Hf1TIYBgqAIBqpnTW2P9")).check(matches(isDisplayed()));
        //}

        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Event> eventList = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String eventID = FirebaseController.getInstance().getUniqueEventID();
            int randomEvent = random.nextInt(50);
            Event newEvent = new Event(userList.get(randomEvent), eventID, "Test Event Number: " + i, null, null, eventID, new Date(), new Date(9223372036854775807L), String.valueOf(i), true);
            FirebaseController.getInstance().addEvent(newEvent);

        }
    }
}
