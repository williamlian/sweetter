package com.codepath.apps.restclienttemplate.adaptor;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.codepath.apps.restclienttemplate.fragment.TimelineFragment;
import com.codepath.apps.restclienttemplate.models.Tweet;

public class HomeTabAdaptor extends FragmentPagerAdapter {
    final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "Timeline", "Mentions" };

    public HomeTabAdaptor(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch(position) {
            case 0:
                return TimelineFragment.newInstance(Tweet.SOURCE_TIMELINE, null);
            case 1:
                return TimelineFragment.newInstance(Tweet.SOURCE_MENTION, null);
        }
        return null;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tabTitles[position];
    }
}
