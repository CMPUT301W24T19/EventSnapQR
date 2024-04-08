package com.example.eventsnapqr;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * starting point for organizing an event, which immediately leads to
 */
public class OrganizeAnEventActivity extends AppCompatActivity implements MapFragmentOrganize.OnLocationPickedListener{

    /**
     * What should be executed when the fragment is created
     *
     * @param savedInstanceState If the fragment is being re-created from
     *                           a previous saved state, this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_organize_an_event);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.organizeEventFragment);
    }

    /**
     * action to be taken when the location has been picked
     * @param latitude chosen latitude
     * @param longitude chosen longitude
     */
    @Override
    public void onLocationPicked(double latitude, double longitude) {
        OrganizeEventFragment fragment = (OrganizeEventFragment) getSupportFragmentManager().findFragmentByTag("YourFragmentTag");
        if (fragment != null) {
            fragment.updateLocationText(latitude, longitude);
        }
    }
}
