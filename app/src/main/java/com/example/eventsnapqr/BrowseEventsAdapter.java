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

    /**
     * create the respective fragment for the tab layout
     * @param position tab index
     * @return fragment associated with the position
     */
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

    /**
     * returns the number of tabs
     * @return 3
     */
    @Override
    public int getItemCount() {
        return 3;
    }
}
