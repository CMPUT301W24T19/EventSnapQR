package com.example.eventsnapqr;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for BrowseEventsActivity to show a fragment in fullscreen, rather than its viewpager
 */
public class FullscreenPagerAdapter extends FragmentStateAdapter {
    private final List<Fragment> fragments = new ArrayList<>();

    public FullscreenPagerAdapter(FragmentManager fragmentManager, Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);
    }

    /**
     * adds a fragment to the adapters list
     * @param fragment the fragment in question
     */
    public void addFragment(Fragment fragment) {
        fragments.add(fragment);
    }

    /**
     * creates the fragment for the given position
     * @param position which tab position is selected (attending or all)
     * @return position
     */
    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragments.get(position);
    }

    /**
     * returns the total number of fragments in the adapter's data set
     * @return number of fragments
     */
    @Override
    public int getItemCount() {
        return fragments.size();
    }
}
