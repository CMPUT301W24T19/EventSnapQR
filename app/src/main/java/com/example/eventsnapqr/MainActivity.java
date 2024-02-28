package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseController firebaseController = FirebaseController.getInstance();

        // Set up NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.mainPageFragment);
        User newUser = new User("John Doe");
        newUser.setHomepage("https://johndoe.com");
        newUser.setContactInfo("john@example.com");
        // Add the user to Firebase
        firebaseController.addUser(newUser);
        // Navigate to MainPageFragment


    }

}