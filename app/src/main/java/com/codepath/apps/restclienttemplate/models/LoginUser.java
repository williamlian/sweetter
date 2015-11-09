package com.codepath.apps.restclienttemplate.models;

import android.content.Context;
import android.util.Log;

import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public class LoginUser implements Serializable {
    int userId;
    String userName;
    String screenName;
    String profileImage;

    private static final String FILE_NAME = "last_logged_in_user";
    private static LoginUser instance;

    private LoginUser(JSONObject user) {
        userId = user.optInt("id");
        userName = user.optString("name");
        screenName = user.optString("screen_name");
        profileImage = user.optString("profile_image_url");
    }

    public static interface LoadLoginCallback {
        void onSuccess();
        void onFailure(Throwable e);
    }

    public static LoginUser get() {
        return instance;
    }

    public static void load(Context context, LoadLoginCallback callback) {
        loadOnline(context, callback);
    }

    private static void loadOnline(final Context context, final LoadLoginCallback callback) {
        RestClient client = RestApplication.getRestClient();
        client.getUserSettings(new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                LoginUser.set(response, context);
                callback.onSuccess();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                Log.e(this.getClass().getName(), "Failed to load login user from online API", throwable);
                try {
                    loadOffline(context);
                    callback.onSuccess();
                } catch (Exception e) {
                    Log.e(this.getClass().getName(),"Failed to load login user from offline file", e);
                    callback.onFailure(throwable);
                }
            }
        });
    }

    private static LoginUser loadOffline(Context context) throws Exception {
        FileInputStream fis = context.openFileInput(FILE_NAME);
        ObjectInputStream is = new ObjectInputStream(fis);
        instance = (LoginUser)is.readObject();
        return instance;
    }

    private static void set(JSONObject user, Context context) {
        instance = new LoginUser(user);
        instance.save(context);
    }

    private void save(Context context) {
        try {
            FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(this);
            os.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
