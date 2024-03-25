package com.example.eventsnapqr;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.view.View;
import android.widget.ImageView;

public class BrowseEventsActivity extends AppCompatActivity {
    private ViewPager2 viewPager;
    private ViewPager2 fullscreenViewPager;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        viewPager = findViewById(R.id.viewPager);
        fullscreenViewPager = findViewById(R.id.fullscreenViewPager);
        tabLayout = findViewById(R.id.tabLayout);

        // Set up the adapter for the main ViewPager2
        viewPager.setAdapter(new BrowseEventsAdapter(this));

        // Set up TabLayout with ViewPager2
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

        fullscreenViewPager.setVisibility(View.GONE);
        ImageView backButton = findViewById(R.id.button_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void switchToFullscreenDetails(String eventId) {
        EventDetailFragment detailsFragment = new EventDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putString("eventId", eventId);
        detailsFragment.setArguments(bundle);
        FullscreenPagerAdapter adapter = new FullscreenPagerAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.addFragment(detailsFragment);
        fullscreenViewPager.setAdapter(adapter);
        fullscreenViewPager.setVisibility(View.VISIBLE);
    }

    public void switchToFullscreenManage(String eventId) {
        ManageEventFragment manageFragment = new ManageEventFragment();
        Bundle bundle = new Bundle();
        bundle.putString("eventId", eventId);
        manageFragment.setArguments(bundle);
        FullscreenPagerAdapter adapter = new FullscreenPagerAdapter(getSupportFragmentManager(), getLifecycle());
        adapter.addFragment(manageFragment);
        fullscreenViewPager.setAdapter(adapter);
        fullscreenViewPager.setVisibility(View.VISIBLE);
    }
}
