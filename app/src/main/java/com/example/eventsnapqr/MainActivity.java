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
    @SuppressLint("HardwareIds") private String androidId;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        FirebaseController firebaseController = FirebaseController.getInstance();
        ContentResolver contentResolver = getBaseContext().getContentResolver();
        androidId = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID);
        FirebaseController.OnUserExistenceCheckedListener listener = new FirebaseController.OnUserExistenceCheckedListener() {
            @Override
            public void onUserExistenceChecked(boolean exists) {
                if (exists) {
                    goToMainPage();
                }
                else if(androidId.equals("123456")){
                    //admin ID -> show admin button
                }
                else {
                    signUp();
                }
            }
        };
        FirebaseController.checkUserExists(androidId, listener);
        /**
        if(firebaseController.checkUserExists(androidId)){
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            navController.navigate(R.id.mainPageFragment);
        }
        else if(firebaseController.isAdmin(androidId)){
            // show admin button
        }
        else{
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            Bundle bundle = new Bundle();
            bundle.putString("userId", androidId);
            navController.navigate(R.id.signUpFragment, bundle);
        }
         **/


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