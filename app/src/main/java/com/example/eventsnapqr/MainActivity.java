package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
    private FirebaseController.Authenticator listener;
    @SuppressLint("HardwareIds") private String androidId;
    @Override
    protected void onResume() {
        super.onResume();
        FirebaseController.checkUserExists(androidId, listener);
    }
    void checkIn(){
        Intent intent = getIntent();
        if (intent != null && Intent.ACTION_VIEW.equals(intent.getAction())) {
            String eventLink = intent.getData().toString();
            Log.d("event link", "link: " + eventLink);
            if (eventLink != null) {
                FirebaseController controller = FirebaseController.getInstance();
                FirebaseController.OnUserRetrievedListener listener = new FirebaseController.OnUserRetrievedListener() {
                    @Override
                    public void onUserRetrieved(User user) {
                        controller.addAttendee(eventLink, user);
                    }
                };
                controller.getUser(androidId, listener); // eventLink uniquely identifies each event document in database
            } else {
                // Do nothing
            }
        }

    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        listener = new FirebaseController.Authenticator() {
            @Override
            public void onUserExistenceChecked(boolean exists) {
                if (exists) {
                    goToMainPage();
                }
                else {
                    signUp();
                }
            }
            @Override
            public void onAdminExistenceChecked(boolean exists) {
                // do nothing
            }
        };
        FirebaseController.checkUserExists(androidId, listener);
        checkIn();
    }
    public void signUp(){
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Bundle bundle = new Bundle();
        bundle.putString("userId", androidId);
        navController.navigate(R.id.signUpFragment, bundle);
    }
    public void goToMainPage(){
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.mainPageFragment);
    }


}