package com.codepath.apps.restclienttemplate;

import android.content.Context;

import com.codepath.apps.restclienttemplate.client.TwitterClient;

/*
 * This is the Android application itself and is used to configure various settings
 * including the image cache in memory and on disk. This also adds a singleton
 * for accessing the relevant rest client.
 *
 *     TwitterClient client = Sweeter.getRestClient();
 *     // use client to send requests to API
 *
 */
public class Sweeter extends com.activeandroid.app.Application {
	private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		Sweeter.context = this;

		//ActiveAndroid.dispose();
		//ActiveAndroid.initialize(context);

	}

	public static TwitterClient getRestClient() {
		return (TwitterClient) TwitterClient.getInstance(TwitterClient.class, Sweeter.context);
	}
}