package com.example.eventsnapqr;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.contrib.RecyclerViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withParentIndex;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anything;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.equalToIgnoringCase;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.is;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.NonNull;
import androidx.test.espresso.ViewAssertion;
import androidx.test.espresso.action.ViewActions;
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

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

public class MassUserTest {
    private List<User> userList;
    private List<Event> eventList;
    private Random random = new Random();
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void init() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();

        CountDownLatch latch = new CountDownLatch(1);
        userList = new ArrayList<>();
        eventList = new ArrayList<>();

        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
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
                                    firebaseFirestore.collection("events").document(doc.getId()).delete();
                                }
                            });
                        }
                    });
                }
            }
        });

        try {
            latch.await(30, TimeUnit.SECONDS);
            Thread.sleep(3000);
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
            latch.await(30, TimeUnit.SECONDS);
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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

    @After
    public void after() {
        eventList.clear();
        userList.clear();
        CountDownLatch latch = new CountDownLatch(1);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
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
                                    firebaseFirestore.collection("events").document(doc.getId()).delete();
                                }
                            });
                        }
                    });
                }
            }
        });

        try {
            latch.await(30, TimeUnit.SECONDS);
            Thread.sleep(3000);
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
            latch.await(30, TimeUnit.SECONDS);
            Thread.sleep(3000);
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
        eventList.sort(Comparator.comparing(o -> o.getEventName().toLowerCase()));
        onView(withId(R.id.browse_events_button)).perform(click());
        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        int i = 0;
        for (Event event: eventList) {
            onData(anything()).inAdapterView(allOf(withId(R.id.events), isDisplayed())).atPosition(i).onChildView(withId(R.id.eventName)).check(matches(withText(event.getEventName())));
            i++;
        }

        try {
            latch.await(10, TimeUnit.SECONDS);
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void
}
