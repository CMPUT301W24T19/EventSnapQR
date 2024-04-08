package com.example.eventsnapqr;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;

/**
 * Activity for Organizers and Attendees to browse and manage active events.
 * Has a tab layout that flips through 3 fragments. Tab layout has three fragments
 * inside a viewpager to display the desired list
 */
public class BrowseEventsActivity extends AppCompatActivity {
    private ViewPager2 viewPager, fullscreenViewPager;
    private TabLayout tabLayout;
    private Integer position;
    private String eventId;


    /**
     * What should be executed when the fragment is created
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);


        Bundle args = getIntent().getExtras();
        if (args != null) {
            position = args.getInt("position");
        } else {
            position = 0;
        }

        // hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        viewPager = findViewById(R.id.viewPager);
        fullscreenViewPager = findViewById(R.id.fullscreenViewPager);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager.setOffscreenPageLimit(3);


        viewPager.setAdapter(new BrowseEventsAdapter(this));
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {
                    switch (position) {
                        case 0:
                            tab.setText("All");
                            break;
                        case 1:
                            tab.setText("Attending");
                            break;
                        case 2:
                            tab.setText("Organized");
                            break;
                    }
                }).attach();
        viewPager.setCurrentItem(position);

        // update tab position for back button functionality
        tabLayout.getTabAt(position).select();
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                position = tab.getPosition();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });


        fullscreenViewPager.setVisibility(View.GONE);
        ImageView backButton = findViewById(R.id.button_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        eventId = getIntent().getStringExtra("eventID"); // for when user enters activity from notification click
        if(eventId != null){
            switchToFullscreenDetails(eventId, true);
        }
    }

    /**
     * Hosts the EventDetailsFragment, making it fullscreen instead of inside the viewpage
     * @param eventId the event being fetched
     * @param toMain flag denoting whether or not to return to main (notifications)
     */
    public void switchToFullscreenDetails(String eventId, Boolean toMain) {
        EventDetailFragment detailsFragment = new EventDetailFragment();
        Bundle bundle = new Bundle();
        if(toMain){
            bundle.putBoolean("toMain", true);

        }
        bundle.putString("eventId", eventId);
        if(position != null){ // for when user enters activity from notification click
            bundle.putInt("position", position);
        }
        detailsFragment.setArguments(bundle);
        FullscreenPagerAdapter adapter = new FullscreenPagerAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.addFragment(detailsFragment);
        fullscreenViewPager.setAdapter(adapter);
        fullscreenViewPager.setVisibility(View.VISIBLE);
    }
}
