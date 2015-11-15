package com.codepath.apps.restclienttemplate.models;


import android.util.Log;

import com.codepath.apps.restclienttemplate.Sweeter;
import com.codepath.apps.restclienttemplate.client.TwitterClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.Serializable;

public class User implements Serializable {
    int userId;
    String userName;
    String screenName;
    String profileImage;
    String profileBackgroundImage;
    int tweetCount;
    int followingCount;
    int followerCount;

    public interface ShowUserCallback {
        void onSuccess(User user);

        void onFailure(Throwable e);
    }

    public User(JSONObject json) {
        this.update(json);
    }

    public void update(JSONObject user) {
        userId = user.optInt("id");
        userName = user.optString("name");
        screenName = user.optString("screen_name");
        profileImage = user.optString("profile_image_url");
        profileBackgroundImage = user.optString("profile_background_image_url");
        tweetCount = user.optInt("statuses_count");
        followerCount = user.optInt("followers_count");
        followingCount = user.optInt("friends_count");
    }

    public static void getUser(final String screenName, final ShowUserCallback callback) {
        TwitterClient client = Sweeter.getRestClient();
        client.showUser(screenName, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                callback.onSuccess(new User(response));
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(this.getClass().getName(), "Failed to load login user from online API", throwable);
                callback.onFailure(throwable);
            }
        });
    }

    public String getUserName() {
        return userName;
    }

    public String getScreenName() {
        return "@" + screenName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getProfileBackgroundImage() {
        return profileBackgroundImage;
    }

    public String getTweetCount() {
        return String.format("%d", tweetCount);
    }

    public String getFollowingCount() {
        return String.format("%d", followingCount);
    }

    public String getFollowerCount() {
        return String.format("%d", followerCount);
    }
}
