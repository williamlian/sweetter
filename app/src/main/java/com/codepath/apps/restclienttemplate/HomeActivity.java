package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
        ViewPager.OnPageChangeListener
{
    HomeTabAdaptor tabAdaptor;
    Fragment currentPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get the ViewPager and set it's PagerAdapter so that it can display items
        ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
        tabAdaptor = new HomeTabAdaptor(getSupportFragmentManager());
        viewPager.setAdapter(tabAdaptor);

        //PagerSlidingTabStrip tabsStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
        //tabsStrip.setViewPager(viewPager);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

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
        currentPage = tabAdaptor.getItem(position);
    }

    @Override
    public void onPageSelected(int position) {
        currentPage = tabAdaptor.getItem(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }
}
