package com.example.eventsnapqr;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * adapter for tab functionality in the browse events activity
 */
public class BrowseEventsAdapter extends FragmentStateAdapter {
    public BrowseEventsAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new ListAllEventsFragment();

            case 1:
                return new ListAttendingEventsFragment();

            case 2:
                return new ListOrganizedEventsFragment();

            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return 3; // Number of tabs
    }
}
