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
        FirebaseController.checkUserExists(androidId, listener);
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        listener = new FirebaseController.Authenticator() {
            @Override
            public void onUserExistenceChecked(boolean exists) {
                if (exists) {
                    //goToMainPage();
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
    }

    /**
     * navigate to the the sign up fragment
     */
    public void signUp(){
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Bundle bundle = new Bundle();
        bundle.putString("userId", androidId);
        navController.navigate(R.id.signUpFragment, bundle);
    }
}