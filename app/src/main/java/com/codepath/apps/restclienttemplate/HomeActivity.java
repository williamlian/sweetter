package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;

import com.codepath.apps.restclienttemplate.adaptor.HomeTabAdaptor;
import com.codepath.apps.restclienttemplate.fragment.ComposeFragment;
import com.codepath.apps.restclienttemplate.fragment.TimelineFragment;
import com.codepath.apps.restclienttemplate.fragment.ViewDetailFragment;
import com.codepath.apps.restclienttemplate.models.LoginUser;
import com.codepath.apps.restclienttemplate.models.User;

public class HomeActivity extends AppCompatActivity
        implements
        ComposeFragment.OnComposeDialogCompleteListenter,
        ViewDetailFragment.OnViewDetailDialogCompleteListener,
        ViewPager.OnPageChangeListener,
        TimelineFragment.OnTimelineActionHandler
{
    HomeTabAdaptor tabAdaptor;
    Fragment currentPage;
    ViewPager viewPager;
    SearchView searchView;
    MenuItem mi_progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabAdaptor = new HomeTabAdaptor(getSupportFragmentManager());
        viewPager.setAdapter(tabAdaptor);
        viewPager.setOnPageChangeListener(this);

        //PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        //tabsStrip.setViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //setupCustomMenu();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline, menu);
        MenuItem searchItem = menu.findItem(R.id.mi_search);
        searchView = (SearchView) MenuItemCompat.getActionView(searchItem);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                search(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });
        mi_progressBar = menu.findItem(R.id.mi_progressBar);

        return super.onCreateOptionsMenu(menu);
    }


    // the below two are mutually exclusive, only use one
    private void setupCustomMenu() {
        ImageButton ib_compose = (ImageButton)findViewById(R.id.ib_compose);
        ib_compose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compose();
            }
        });
        ImageButton ib_profile = (ImageButton)findViewById(R.id.ib_profile);
        ib_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProfile(LoginUser.get());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_compose:
                compose();
                return true;
            case R.id.mi_profile:
                showProfile(LoginUser.get());
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /* *********************************************************************************************
     *
     * User Actions
     *
     * *********************************************************************************************/
    private void compose() {
        FragmentManager fm = getSupportFragmentManager();
        ComposeFragment composeDialog = ComposeFragment.newInstance();
        composeDialog.show(fm, "compose_dialog");
    }

    private void showProfile(User user) {
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.putExtra(ProfileActivity.ARGS_USER, user.getScreenName());
        startActivity(profileIntent);
    }

    private void search(String search) {
        viewPager.setCurrentItem(0, true);
        TimelineFragment timeline = (TimelineFragment)tabAdaptor.getItem(0);
        timeline.search(search);
    }

    /* *********************************************************************************************
     *
     * Dialog Handlers
     *
     * *********************************************************************************************/
    @Override
    public void onPost() {
        if(currentPage instanceof TimelineFragment) {
            ((TimelineFragment)currentPage).getTimelineFromLocal();
        }
    }

    @Override
    public void onDetailViewComplete(boolean refreshNeeded) {
        if(refreshNeeded) {
            Log.i(getClass().getName(), "Refreshing Timeline");
            if(currentPage instanceof TimelineFragment) {
                ((TimelineFragment)currentPage).getTimelineFromLocal();
            }
        }
    }

    /* *********************************************************************************************
     *
     * Page View Handlers
     *
     * *********************************************************************************************/
    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
        //currentPage = tabAdaptor.getItem(position);
        //.i(getClass().getName(), "Tab scrolled: " + currentPage.toString());
    }

    @Override
    public void onPageSelected(int position) {
        currentPage = tabAdaptor.getItem(position);
        Log.i(getClass().getName(), "Tab selected: " + currentPage.toString());
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /* *********************************************************************************************
     *
     * Timeline Handlers
     *
     * *********************************************************************************************/
    @Override
    public void onTimelineLoadStart() {
        if(mi_progressBar != null) {
            mi_progressBar.setVisible(true);
        }
    }

    @Override
    public void onTimelineLoadCompleted() {
        if(mi_progressBar != null) {
            mi_progressBar.setVisible(false);
        }
    }
}
