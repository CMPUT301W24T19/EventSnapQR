package com.example.eventsnapqr;

//import org.junit.Rule;

import android.app.Activity;

import androidx.test.rule.ActivityTestRule;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
//import org.junit.Rule;
//@RunWith(AndroidJUnit4.class)
public class AuthenticationTest {

    @Before
    public void init(){
        Activity activity = mainActivityActivityTestRule.getActivity();
    }
    @Test
    public void identityTest(){
        FirebaseController firebaseController = new FirebaseController();

    }





    @Rule
    public ActivityTestRule<MainActivity> mainActivityActivityTestRule =
            new ActivityTestRule<>(MainActivity.class, true, true);



}
