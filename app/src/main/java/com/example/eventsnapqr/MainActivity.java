package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseController firebaseController = FirebaseController.getInstance();
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        @SuppressLint("HardwareIds") String androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        if(firebaseController.checkUserExists(androidId)){
            // the user already exists, do stuff
        }else{
            // sign up
        }
        // Set up NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.mainPageFragment);

    }

}