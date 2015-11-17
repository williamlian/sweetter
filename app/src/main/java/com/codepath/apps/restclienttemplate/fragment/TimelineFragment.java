package com.codepath.apps.restclienttemplate.fragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.Sweeter;
import com.codepath.apps.restclienttemplate.adaptor.TweetAdaptor;
import com.codepath.apps.restclienttemplate.client.TwitterClient;
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

public class TimelineFragment extends Fragment implements TweetAdaptor.OnReplyHandler
{
    private static final int HTTP_TOO_MANY_REQUESTS = 429;
    static final boolean GET_USER_TIMELINE = false;
    static final String USER_OVERRIDE = null; //"Android"; //debug purpose, set this to null when deploying
    static final int FETCH_SIZE = 100;
    public static final String ARGS_SOURCE = "source";
    public static final String ARGS_USER = "user";

    // UI widget handle
    TweetAdaptor tweetAdaptor;
    SwipeRefreshLayout swipeContainer;

    /* noMoreTweets will be true if:
     *   - maxId returned is the same as the max Id from the list
     *   - HTTP 429 : Too Many Requests returned
     *   - No network connection
     * Every time call to getTimeline(null) will reset this flag to true */
    boolean noMoreTweets = false;

    /*
     * The source of timeline, possible values listed in Tweet class */
    String source;

    /*
     * If source == USER then the user's screen name, otherwise ignored */
    String user = null;

    /*
     * If source == SEARCH then the search query, otherwise ignored */
    String query = null;

    /* *********************************************************************************************
     *
     * Fragment Hanlder
     *
     * *********************************************************************************************/

    public interface OnTimelineActionHandler {
        void onTimelineLoadStart();
        void onTimelineLoadCompleted();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(!(activity instanceof OnTimelineActionHandler)) {
            throw new TypeNotPresentException("OnTimelineActionHandler",null);
        }
        actionHandler = (OnTimelineActionHandler)activity;
    }

    private OnTimelineActionHandler actionHandler;

    /* *********************************************************************************************
     *
     * Fragment OVerrides
     *
     * *********************************************************************************************/
    public static TimelineFragment newInstance(String source, String user) {
        Bundle args = new Bundle();
        args.putString(ARGS_SOURCE, source);
        args.putString(ARGS_USER, user);
        TimelineFragment fragment = new TimelineFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public String toString() {
        return String.format("Timeline Fragment for %s", source);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        source = getArguments().getString(ARGS_SOURCE);
        user = getArguments().getString(ARGS_USER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_timeline, container, false);

        tweetAdaptor = new TweetAdaptor(getActivity(), new ArrayList<Tweet>(), this);
        ListView ll_timeline = (ListView) view.findViewById(R.id.ll_timeline);
        ll_timeline.setAdapter(tweetAdaptor);
        ll_timeline.setOnScrollListener(onLoadMoreListener);
        ll_timeline.setOnItemClickListener(onItemClickListener);

        swipeContainer = (SwipeRefreshLayout) view.findViewById(R.id.srl_timeline);
        swipeContainer.setOnRefreshListener(swipeLoader);
        swipeContainer.setColorSchemeResources(R.color.twitter_blue);

        getTimeline(null);

        return view;
    }

    /* *********************************************************************************************
     *
     * Main Loader - all request go from here
     *
     * *********************************************************************************************/
    public void getTimeline(String max) {
        if(AppUtil.isNetworkAvailable(getContext())) {
            actionHandler.onTimelineLoadStart();
            if (GET_USER_TIMELINE) {
                getUserTimeline(max);
            } else {
                switch(source) {
                    case Tweet.SOURCE_TIMELINE:
                        getHomeTimeline(max);
                        break;
                    case Tweet.SOURCE_MENTION:
                        getMentionTimeline(max);
                        break;
                    case Tweet.SOURCE_USER:
                        getUserTimeline(max);
                        break;
                    case Tweet.SOURCE_SEARCH:
                        getSearchTimeline(max);
                        break;
                }
            }
        } else {
            getTimelineFromLocal();
        }
    }

    public void getTimelineFromLocal() {
        List<Tweet> tweets = Tweet.fromLocal(source);
        tweetAdaptor.clear();
        tweetAdaptor.addAll(tweets);
    }

    public void search(String query) {
        if(query != null && !query.isEmpty()) {
            this.source = Tweet.SOURCE_SEARCH;
            this.query = query;
        } else {
            this.source = Tweet.SOURCE_TIMELINE;
            this.query = null;
        }
        getTimeline(null);
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
        TwitterClient client = Sweeter.getRestClient();
        client.logRateLimit();
        client.getHomeTimeline(FETCH_SIZE, max, new TimelineResponseHandler(max));
    }

    private void getMentionTimeline(String max) {
        Log.i(this.getClass().getName(), "Loading mention timeline older than: " + max);
        TwitterClient client = Sweeter.getRestClient();
        client.logRateLimit();
        client.getMentionTimeline(FETCH_SIZE, max, new TimelineResponseHandler(max));
    }

    private void getUserTimeline(String max) {
        String screenName = user;
        if(screenName == null) {
            //default to the current login user
            screenName = LoginUser.get().getScreenName();
        }
        Log.i(this.getClass().getName(), String.format("Loading user timeline for %s older than: %s", screenName, max));
        TwitterClient client = Sweeter.getRestClient();
        client.logRateLimit();
        client.getUserTimeline(screenName, FETCH_SIZE, max, new TimelineResponseHandler(max));
    }

    private void getSearchTimeline(String max) {
        Log.i(this.getClass().getName(), String.format("Loading search timeline for %s older than: %s", query, max));
        TwitterClient client = Sweeter.getRestClient();
        client.searchTimeline(query, FETCH_SIZE, max, new TimelineResponseHandler(max));
    }

    /* *********************************************************************************************
     *
     * Response Handlers
     *
     * *********************************************************************************************/
    private class TimelineResponseHandler extends JsonHttpResponseHandler {
        private String maxId;
        public TimelineResponseHandler(String maxId) {
            this.maxId = maxId;
        }

        public void onSuccess(int statusCode, Header[] headers, JSONArray jsonArray) {
            if (maxId == null) {
                noMoreTweets = false;
                Tweet.clearLocal(Tweet.SOURCE_TIMELINE);
                tweetAdaptor.clear();
            }
            List<Tweet> tweets = Tweet.fromJson(jsonArray, Tweet.SOURCE_TIMELINE);
            int prevSize = tweetAdaptor.getCount();
            Log.i(this.getClass().getName(),
                    String.format("%d tweets loaded, max_id = %s",tweets.size(),tweets.get(tweets.size()-1).getTweetId()));

            //only add older tweets
            for(Tweet tweet : tweets) {
                if(tweet.getTweetId().equals(maxId)) {
                    continue;
                }
                tweetAdaptor.add(tweet);
            }
            if(tweetAdaptor.getCount() == prevSize) {
                noMoreTweets = true;
            }
            swipeContainer.setRefreshing(false);
            actionHandler.onTimelineLoadCompleted();
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            onSuccess(statusCode, headers, response.optJSONArray("statuses"));
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject json) {
            if(statusCode == 0) {
                if(maxId == null) {
                    Log.e(this.getClass().getName(), "No network, load offline");
                    Toast.makeText(getContext(), "No network available, loading offline content", Toast.LENGTH_LONG).show();
                    getTimelineFromLocal();
                }
            } else if(statusCode == HTTP_TOO_MANY_REQUESTS) {
                Toast.makeText(getContext(), "API rate limit exceeded, please wait some time then refresh", Toast.LENGTH_SHORT).show();
                if(maxId == null) {
                    tweetAdaptor.clear();
                    tweetAdaptor.addAll(Tweet.fromLocal(Tweet.SOURCE_TIMELINE));
                } else {
                    noMoreTweets = true;
                }
            } else {
                showError(AppUtil.parseJsonError(json));
                Log.e(this.getClass().getName(), String.format("Rest call failed [%d] %s", statusCode, json.toString()));
            }
            swipeContainer.setRefreshing(false);
            actionHandler.onTimelineLoadCompleted();
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

    private void showDetailView(Tweet tweet) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ViewDetailFragment detailFragment = ViewDetailFragment.newInstance(tweet, false);
        detailFragment.show(fm, "detail_view_dialog");
    }

    @Override
    public void onReply(Tweet tweet) {
        FragmentManager fm = getActivity().getSupportFragmentManager();
        ViewDetailFragment detailFragment = ViewDetailFragment.newInstance(tweet, true);
        detailFragment.show(fm, "detail_view_dialog");
    }

    private void showError(String message) {
        new MaterialDialog.Builder(getContext())
                .title("Failed to load Timeline")
                .content(message)
                .positiveText("Ok")
                .show();
    }
}
