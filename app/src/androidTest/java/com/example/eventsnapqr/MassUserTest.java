package com.example.eventsnapqr;

import android.content.ContentResolver;
import android.content.Context;
import android.provider.Settings;
import android.text.format.DateUtils;
import android.util.Log;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Firebase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
import java.util.concurrent.TimeUnit;

public class MassUserTest {
    @Rule
    public ActivityScenarioRule<MainActivity> activityRule = new ActivityScenarioRule<>(MainActivity.class);

    @Before
    public void init() {
        Context context = InstrumentationRegistry.getInstrumentation().getContext();
        ContentResolver contentResolver = context.getContentResolver();

        CountDownLatch latch = new CountDownLatch(1);
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        /*firebaseFirestore.collection("users").get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
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
        });*/
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
                                    firebaseFirestore.collection("users").document(doc.getId()).delete();
                                }
                            });
                        }
                    });
                }
            }
        });

        try {
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

    @Test
    public void testTest() {
        Log.d("TAG", "true");
    }

}
