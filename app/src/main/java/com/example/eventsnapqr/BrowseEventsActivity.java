package com.example.eventsnapqr;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import android.view.View;
import android.widget.ImageView;

public class BrowseEventsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_events);

        // Hide the action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        ViewPager2 viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(new BrowseEventsAdapter(this));

        TabLayout tabLayout = findViewById(R.id.tabLayout);
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

        // ImageView back button click listener
        ImageView backButton = findViewById(R.id.button_back_button);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Finish the activity when back button is pressed
                finish();
            }
        });
    }
}
