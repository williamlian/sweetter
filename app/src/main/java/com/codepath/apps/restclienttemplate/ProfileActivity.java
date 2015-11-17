package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codepath.apps.restclienttemplate.fragment.SendDirectMessageFragment;
import com.codepath.apps.restclienttemplate.fragment.TimelineFragment;
import com.codepath.apps.restclienttemplate.fragment.ViewDirectMessageFragment;
import com.codepath.apps.restclienttemplate.models.LoginUser;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.models.User;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity
        implements User.ShowUserCallback, TimelineFragment.OnTimelineActionHandler
{
    public static final String ARGS_USER = "screen_name";

    private User user;
    private MenuItem mi_progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String screenName = getIntent().getExtras().getString(ARGS_USER);
        User.getUser(screenName,this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile, menu);
        mi_progressBar = menu.findItem(R.id.mi_progressBar);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mi_directMessage:
                if(user.getScreenName().equals(LoginUser.get().getScreenName())) {
                    viewDirectMessage();
                } else {
                    composeDirectMessage();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void onLoadUserCompleted() {
        ImageView iv_profileBackground = (ImageView)findViewById(R.id.iv_profileBackground);
        ImageView iv_profileImage = (ImageView)findViewById(R.id.iv_userProfile);
        TextView tv_userName = (TextView)findViewById(R.id.tv_userName);
        TextView tv_screenName = (TextView)findViewById(R.id.tv_screenName);

        TextView tv_tweets = (TextView)findViewById(R.id.tv_tweets);
        TextView tv_following = (TextView)findViewById(R.id.tv_following);
        TextView tv_follower = (TextView)findViewById(R.id.tv_follower);

        Picasso.with(this).load(user.getProfileBackgroundImage()).into(iv_profileBackground);
        Picasso.with(this).load(user.getProfileImage()).into(iv_profileImage);
        tv_userName.setText(user.getUserName());
        tv_screenName.setText(user.getScreenName());

        tv_tweets.setText(getCountSpan(user.getTweetCount(), "TWEETS"));
        tv_following.setText(getCountSpan(user.getFollowingCount(), "FOLLOWING"));
        tv_follower.setText(getCountSpan(user.getFollowerCount(), "FOLLOWER"));

        tv_following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFollowing();
            }
        });
        tv_follower.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFollower();
            }
        });

        TimelineFragment timelineFragment = TimelineFragment.newInstance(Tweet.SOURCE_USER, user.getScreenName());
        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.fl_timeline, timelineFragment).commit();
    }

    /* *********************************************************************************************
     *
     * Implemented Callbacks
     *
     * *********************************************************************************************/
    @Override
    public void onSuccess(User user) {
        this.user = user;
        onLoadUserCompleted();
    }

    @Override
    public void onFailure(Throwable e) {
        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(e.getMessage())
                .positiveText("Ok")
                .show();
    }

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

    /* *********************************************************************************************
     *
     * UI Actions
     *
     * *********************************************************************************************/
    private void showFollowing() {
        Intent showUsersIntent = new Intent(this, ShowFollowActivity.class);
        showUsersIntent.putExtra(ShowFollowActivity.ARG_SCREEN_NAME, user.getScreenName());
        showUsersIntent.putExtra(ShowFollowActivity.ARG_USER_LIST_TYPE, ShowFollowActivity.FOLLOWINGS);
        startActivity(showUsersIntent);
    }

    private void showFollower() {
        Intent showUsersIntent = new Intent(this, ShowFollowActivity.class);
        showUsersIntent.putExtra(ShowFollowActivity.ARG_SCREEN_NAME, user.getScreenName());
        showUsersIntent.putExtra(ShowFollowActivity.ARG_USER_LIST_TYPE, ShowFollowActivity.FOLLOWERS);
        startActivity(showUsersIntent);
    }

    private void composeDirectMessage() {
        FragmentManager fm = getSupportFragmentManager();
        SendDirectMessageFragment fragment = SendDirectMessageFragment.newInstance(user);
        fragment.show(fm, "send_direct_message");
    }

    private void viewDirectMessage() {
        FragmentManager fm = getSupportFragmentManager();
        ViewDirectMessageFragment viewDirectMessageFragment = ViewDirectMessageFragment.newInstance();
        viewDirectMessageFragment.show(fm, "view_direct_message_dialog");
    }

    /* *********************************************************************************************
     *
     * UI Helper
     *
     * *********************************************************************************************/
    private SpannableStringBuilder getCountSpan(String count, String text) {
        ForegroundColorSpan countColor = new ForegroundColorSpan(getResources().getColor(R.color.user_name));
        SpannableStringBuilder ssb = new SpannableStringBuilder(count);
        ssb.setSpan(countColor, 0, ssb.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(Html.fromHtml("<br/>"));

        ForegroundColorSpan textSpan = new ForegroundColorSpan(getResources().getColor(R.color.regular));
        ssb.append(text);
        ssb.setSpan(textSpan, ssb.length() - text.length(),ssb.length(),Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }
}
