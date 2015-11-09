package com.codepath.apps.restclienttemplate.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.json.JSONArray;
import org.json.JSONObject;

public class AppUtil {
    public static Boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting();
    }

    public static String parseJsonError(JSONObject json) {
        JSONArray errors = json.optJSONArray("errors");
        StringBuilder sb = new StringBuilder();
        if (errors != null) {
            sb.append("Errors: \n");
            for (int i = 0; i < errors.length(); i++) {
                JSONObject error = errors.optJSONObject(i);
                sb.append(error.optString("code")).append(" - ").append(error.optString("message")).append("\n");
            }
            sb.deleteCharAt(sb.length() - 1); //remove the last newline
        } else {
            sb.append("Errors: ").append(json.toString());
        }
        return sb.toString();
    }
}
