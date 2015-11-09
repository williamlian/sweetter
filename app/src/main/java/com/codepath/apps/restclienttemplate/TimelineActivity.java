package com.codepath.apps.restclienttemplate;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codepath.apps.restclienttemplate.adaptor.TweetAdaptor;
import com.codepath.apps.restclienttemplate.client.TwitterClient;
import com.codepath.apps.restclienttemplate.fragment.ComposeFragment;
import com.codepath.apps.restclienttemplate.fragment.ViewDetailFragment;
import com.codepath.apps.restclienttemplate.models.LoginUser;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.util.AppUtil;
import com.codepath.apps.restclienttemplate.widget.EndlessScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class TimelineActivity extends AppCompatActivity implements ComposeFragment.OnComposeDialogCompleteListenter {
    TweetAdaptor tweetAdaptor;
    SwipeRefreshLayout swipeContainer;

    boolean noMoreTweets = false;

    static final boolean GET_USER_TIMELINE = true;
    static final boolean USE_COMPOSE_DIALOG = true; //set to false to use activity window

    static final String USER_OVERRIDE = null; //"Android"; //debug purpose, set this to null when deploying

    static final int REQUEST_COMPOSE = 100;
    static final int FETCH_SIZE = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayShowTitleEnabled(false);

        tweetAdaptor = new TweetAdaptor(this, new ArrayList<Tweet>());
        ListView ll_timeline = (ListView) findViewById(R.id.ll_timeline);
        ll_timeline.setAdapter(tweetAdaptor);
        ll_timeline.setOnScrollListener(onLoadMoreListener);
        ll_timeline.setOnItemClickListener(onItemClickListener);

        swipeContainer = (SwipeRefreshLayout) findViewById(R.id.srl_timeline);
        swipeContainer.setOnRefreshListener(swipeLoader);
        swipeContainer.setColorSchemeResources(R.color.twitter_blue);

        ImageButton ib_compose = (ImageButton) findViewById(R.id.ib_compose);
        ib_compose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                compose();
            }
        });

        getTimeline(null);
    }

    /* *********************************************************************************************
     *
     * Main Loader - all request go from here
     *
     * *********************************************************************************************/
    public void getTimeline(String max) {
        if(AppUtil.isNetworkAvailable(this)) {
            if (GET_USER_TIMELINE) {
                getUserTimeline(max);
            } else {
                getHomeTimeline(max);
            }
        } else {
            getTimelineFromLocal();
        }
    }

    /* *********************************************************************************************
     *
     * UI Method Triggering Reloads
     *
     * *********************************************************************************************/
    private EndlessScrollListener onLoadMoreListener = new EndlessScrollListener() {
        @Override
        public boolean onLoadMore (int page, int totalItemsCount) {
            if (tweetAdaptor.getCount() == 0) {
                getTimeline(null);
            } else {
                if(!noMoreTweets) {
                    String lastId = tweetAdaptor.getItem(tweetAdaptor.getCount() - 1).getTweetId();
                    getTimeline(lastId);
                } else {
                    return  false;
                }
            }
            return true;
        }
    };

    //reload tweets
    private SwipeRefreshLayout.OnRefreshListener swipeLoader = new SwipeRefreshLayout.OnRefreshListener() {
        @Override
        public void onRefresh() {
            getTimeline(null);
        }
    };

    /* *********************************************************************************************
     *
     * Network Requests
     *
     * *********************************************************************************************/
    private void getHomeTimeline(String max) {
        Log.i(this.getClass().getName(), "Loading timeline older than: " + max);
        TwitterClient client = RestApplication.getRestClient();
        client.logRateLimit();
        client.getHomeTimeline(FETCH_SIZE, max, new TimelineResponseHandler(max));
    }

    private void getUserTimeline(String max) {
        String screenName = USER_OVERRIDE;
        if(screenName == null) {
            screenName = LoginUser.get().getScreenName();
        }
        Log.i(this.getClass().getName(), String.format("Loading user timeline for %s older than: %s",screenName,max));
        TwitterClient client = RestApplication.getRestClient();
        client.logRateLimit();
        client.getUserTimeline(screenName, FETCH_SIZE, max, new TimelineResponseHandler(max));
    }

    private void getTimelineFromLocal() {
        Toast.makeText(this, "No network available, loading offline content", Toast.LENGTH_LONG).show();
        List<Tweet> tweets = Tweet.fromLocal();
        tweetAdaptor.clear();
        tweetAdaptor.addAll(tweets);
    }

    /* *********************************************************************************************
     *
     * Response Handlers
     *
     * *********************************************************************************************/
    private class TimelineResponseHandler extends JsonHttpResponseHandler{
        private String maxId;
        public TimelineResponseHandler(String maxId) {
            this.maxId = maxId;
        }

        public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
            if (maxId == null) {
                noMoreTweets = false;
                Tweet.clearLocal();
                tweetAdaptor.clear();
            }
            List<Tweet> tweets = Tweet.fromJson(jsonArray);
            int prevSize = tweets.size();
            Log.i(this.getClass().getName(),String.format("%d tweets loaded, max_id = %s",tweets.size(),tweets.get(tweets.size()-1).getTweetId()));

            //only add older tweets
            for(Tweet tweet : tweets) {
                if(tweet.getTweetId().equals(maxId)) {
                    continue;
                }
                tweetAdaptor.add(tweet);
            }
            if(tweets.size() == prevSize) {
                noMoreTweets = true;
            }
            swipeContainer.setRefreshing(false);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
            showError(responseString);
            Log.e(this.getClass().getName(), String.format("Rest call failed [%d] %s", statusCode, responseString));
            swipeContainer.setRefreshing(false);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
            if(statusCode == 0) {
                if(maxId == null) {
                    Log.e(this.getClass().getName(), "No network, load offline");
                    getTimelineFromLocal();
                }
            }
            else {
                showError(AppUtil.parseJsonError(json));
                Log.e(this.getClass().getName(), String.format("Rest call failed [%d] %s", statusCode, json.toString()));
            }
            swipeContainer.setRefreshing(false);
        }
    };


    private AdapterView.OnItemClickListener onItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            Tweet tweet = (Tweet)parent.getItemAtPosition(position);
            showDetailView(tweet);
        }
    };

    /* *********************************************************************************************
     *
     * UI Methods
     *
     * *********************************************************************************************/
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //MenuInflater inflater = getMenuInflater();
        //inflater.inflate(R.menu.timeline, menu);
        return super.onCreateOptionsMenu(menu);
    }

    private void compose() {
        if(USE_COMPOSE_DIALOG) {
            FragmentManager fm = getSupportFragmentManager();
            ComposeFragment composeDialog = ComposeFragment.newInstance();
            composeDialog.show(fm, "compose_dialog");
        } else {
            Intent composeIntent = new Intent(this, ComposeActivity.class);
            startActivityForResult(composeIntent, REQUEST_COMPOSE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_COMPOSE) {
            if(resultCode == RESULT_OK) {
                getTimelineFromLocal();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPost() {
        getTimelineFromLocal();
    }

    public void showDetailView(Tweet tweet) {
        FragmentManager fm = getSupportFragmentManager();
        ViewDetailFragment detailFragment = ViewDetailFragment.newInstance(tweet);
        detailFragment.show(fm, "detail_view_dialog");
    }

    private void showError(String message) {
        new MaterialDialog.Builder(this)
                .title("Failed to load Timeline")
                .content(message)
                .positiveText("Ok")
                .show();
    }

}
