package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.os.Bundle;
import android.provider.Settings;

public class MainActivity extends AppCompatActivity {
    private FirebaseController.Authenticator listener;
    @SuppressLint("HardwareIds") private String androidId;
    @Override
    protected void onResume() {
        super.onResume();
        FirebaseController.checkUserExists(androidId, listener);
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
                if(exists){
                    adminMode();
                }
            }
        };
        FirebaseController.checkUserExists(androidId, listener);
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
    public void adminMode(){
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        Bundle bundle = new Bundle();
        bundle.putBoolean("Admin",true);
        navController.navigate(R.id.mainPageFragment, bundle);
    }


}