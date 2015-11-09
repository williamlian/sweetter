package com.codepath.apps.restclienttemplate;

import android.content.Context;
import android.util.Log;

import com.codepath.oauth.OAuthBaseClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;
import org.scribe.builder.api.Api;
import org.scribe.builder.api.TwitterApi;

import java.util.Iterator;

/*
 * 
 * This is the object responsible for communicating with a REST API. 
 * Specify the constants below to change the API being communicated with.
 * See a full list of supported API classes: 
 *   https://github.com/fernandezpablo85/scribe-java/tree/master/src/main/java/org/scribe/builder/api
 * Key and Secret are provided by the developer site for the given API i.e dev.twitter.com
 * Add methods for each relevant endpoint in the API.
 * 
 * NOTE: You may want to rename this object based on the service i.e TwitterClient or FlickrClient
 * 
 */
public class RestClient extends OAuthBaseClient {
	public static final Class<? extends Api> REST_API_CLASS = TwitterApi.class;
	public static final String REST_URL = "https://api.twitter.com/1.1";
	public static final String REST_CONSUMER_KEY = "SdqOBMn0AmTYvmxI8wKnBVE3W";
	public static final String REST_CONSUMER_SECRET = "eEUT2e2IqYdJFi65vihDiayU7JNBsovokNwPfvYn0chCGY6sly";
	public static final String REST_CALLBACK_URL = "oauth://sweetter.williamlian.com";

	public RestClient(Context context) {
		super(context, REST_API_CLASS, REST_URL, REST_CONSUMER_KEY, REST_CONSUMER_SECRET, REST_CALLBACK_URL);
	}

	public void getHomeTimeline(int count, String maxId, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/home_timeline.json");
		RequestParams params = new RequestParams();
		params.put("count", String.valueOf(count));
        if(maxId != null) {
            params.put("max_id", maxId);
        }
        params.put("exclude_replies", "1");
        getClient().get(apiUrl, params, handler);
	}

    public void getUserTimeline(String screenName, int count, String maxId, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("statuses/user_timeline.json");
        RequestParams params = new RequestParams();
        params.put("screen_name", screenName);
        params.put("count", String.valueOf(count));
        if(maxId != null) {
            params.put("max_id", maxId);
        }
        params.put("exclude_replies", "1");
        getClient().get(apiUrl, params, handler);
    }

    public void getUserSettings(final JsonHttpResponseHandler handler) {
        String apiUrl = getApiUrl("account/settings.json");
        getClient().get(apiUrl, new RequestParams(),new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                String screenName = response.optString("screen_name");
                if (screenName != null) {
                    Log.i(this.getClass().getName(), "Login user: " + screenName);
                    showUser(screenName, handler);
                } else {
                    Log.e(this.getClass().getName(), "cannot get login user screen name");
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                handler.onFailure(statusCode, headers, throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                handler.onFailure(statusCode, headers, responseString, throwable);
            }
        });
    }

    public void showUser(String screenName, AsyncHttpResponseHandler handler) {
        String apiUrl = getApiUrl("users/show.json");
        RequestParams params = new RequestParams();
        params.put("screen_name", screenName);
        getClient().get(apiUrl, params, handler);
    }

	public void postTweet(String body, AsyncHttpResponseHandler handler) {
		String apiUrl = getApiUrl("statuses/update.json");
		RequestParams params = new RequestParams();
		params.put("status", body);
		getClient().post(apiUrl, params, handler);
	}

    public void logRateLimit() {
        String apiUrl = getApiUrl("application/rate_limit_status.json");
        RequestParams params = new RequestParams();
        params.put("resources","statuses");
        getClient().get(apiUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    JSONObject statuses = response.getJSONObject("resources").getJSONObject("statuses");
                    Iterator<String> keys = statuses.keys();
                    while(keys.hasNext()) {
                        String key = keys.next();
                        if(key.contains("timeline")) {
                            String limit = statuses.getJSONObject(key).getString("limit");
                            String remaining = statuses.getJSONObject(key).getString("remaining");
                            Log.i(RestClient.class.getName(), String.format("Twitter API Rate Check: %s [%s/%s]", key, limit, remaining));
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
}