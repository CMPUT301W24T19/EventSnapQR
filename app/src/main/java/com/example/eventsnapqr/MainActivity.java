package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * the starting point of the application. if the userId is not in the data base, this
 * activity will lead to the sign up page, otherwise it will lead to the main page
 */
public class MainActivity extends AppCompatActivity {
    private FirebaseController.Authenticator listener;
    private String androidId;

    /**
     * executed if the activity is restarted
     */
    @Override
    protected void onResume() {
        super.onResume();
        String fragmentToLoad = getIntent().getStringExtra("fragmentToLoad");
        if ("ViewUserProfileFragment".equals(fragmentToLoad)) {
            // Retrieve additional data if needed
            String attendeeId = getIntent().getStringExtra("attendeeId");
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("attendeeId", attendeeId);
            navController.navigate(R.id.action_global_viewUserProfileFragment, bundle);
            // Clear the intent to prevent reloading the fragment on subsequent activity restarts
            getIntent().removeExtra("fragmentToLoad");
            getIntent().removeExtra("attendeeId");
        }
        FirebaseController.checkUserExists(androidId, listener);
        FirebaseController firebaseController = new FirebaseController();
        FirebaseController.AttendeeCheckCallback attendeeCheckCallback = new FirebaseController.AttendeeCheckCallback() {
            @Override
            public void onChecked(boolean isAttendee, Event event) {
                if(isAttendee){
                    firebaseController.listenForAnnouncements(getApplicationContext(), event);
                }
            }
        };
        firebaseController.getAllEvents(new FirebaseController.OnEventsLoadedListener() {
            @Override
            public void onEventsLoaded(ArrayList<Event> events) {
                for(Event event: events){
                    firebaseController.isAttendee(androidId, event, attendeeCheckCallback);
                }
            }
        });
    }

    /**
     *
     * @param savedInstanceState If the activity is being re-initialized after
     *     previously being shut down then this Bundle contains the data it most
     *     recently supplied in {@link #onSaveInstanceState}.  <b><i>Note: Otherwise it is null.</i></b>
     *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ContentResolver contentResolver = getBaseContext().getContentResolver();
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        listener = new FirebaseController.Authenticator() {
            @Override
            public void onUserExistenceChecked(boolean exists) {
                if (!exists) {
                    User newUser = new User(androidId, androidId);
                    FirebaseController.getInstance().addUser(newUser);
                }
            }
            @Override
            public void onAdminExistenceChecked(boolean exists) {
                // do nothing
            }
        };
        FirebaseController.checkUserExists(androidId, listener);
    }
}