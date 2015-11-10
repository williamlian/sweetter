package com.codepath.apps.restclienttemplate.util;

import android.content.Context;

import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.client.TwitterClient;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

public class TweetAction {
    public static final int REPLY = 0;
    public static final int RETWEET = 1;
    public static final int FAVORITE = 2;

    Context context;

    public TweetAction(Context context) {
        this.context = context;
    }

    public void reply(Tweet tweet, String replyText, TweetActionCallback callback) {
        TwitterClient client = RestApplication.getRestClient();
        client.reply(tweet.getTweetId(), replyText, new ActionResponseHandler(REPLY, tweet, callback));
    }

    public void retweet(Tweet tweet, TweetActionCallback callback) {
        TwitterClient client = RestApplication.getRestClient();
        client.retweet(tweet.getTweetId(), new ActionResponseHandler(RETWEET, tweet, callback));
    }

    public void favorite(Tweet tweet, TweetActionCallback callback) {
        TwitterClient client = RestApplication.getRestClient();
        client.favorite(tweet.getTweetId(), new ActionResponseHandler(FAVORITE, tweet, callback));
    }

    public interface TweetActionCallback {
        void onReply(Tweet tweet);
        void onRetweet(Tweet tweet);
        void onFavorite(Tweet tweet);
        void onTweetActionFailure(int actionType, int status, String error);
    }

    private class ActionResponseHandler extends JsonHttpResponseHandler {
        private int actionType;
        private TweetActionCallback callback;
        private Tweet tweet;

        public ActionResponseHandler(int actionType, Tweet tweet, TweetActionCallback callback) {
            this.actionType = actionType;
            this.callback = callback;
            this.tweet = tweet;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

            switch (actionType) {
                case REPLY:
                    Tweet reply = new Tweet(response);
                    reply.save();
                    callback.onReply(tweet);
                    break;
                case RETWEET:
                    tweet.retweet();
                    tweet.save();
                    callback.onRetweet(tweet);
                    break;
                case FAVORITE:
                    tweet.favorite();
                    tweet.save();
                    callback.onFavorite(tweet);
                    break;
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            if(statusCode == 0) {
                callback.onTweetActionFailure(actionType, statusCode, "Network Error");
            } else {
                callback.onTweetActionFailure(actionType, statusCode, AppUtil.parseJsonError(errorResponse));
            }
        }
    }
}
