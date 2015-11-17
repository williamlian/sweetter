package com.codepath.apps.restclienttemplate.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DirectMessage {
    String id;
    String senderUserName;
    String senderScreenName;
    String senderProfileImage;
    String message;

    public DirectMessage(JSONObject json) {
        this.id = json.optString("id_str");
        this.senderProfileImage = json.optJSONObject("sender").optString("profile_image_url");
        this.senderUserName = json.optJSONObject("sender").optString("name");
        this.senderScreenName = json.optString("sender_screen_name");
        this.message = json.optString("text");
    }

    public static List<DirectMessage> getList(JSONArray messages) {
        List<DirectMessage> dmList = new ArrayList<>();
        for(int i = 0; i < messages.length(); i++) {
            try {
                dmList.add(new DirectMessage(messages.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return dmList;
    }

    public String getId() {
        return id;
    }

    public String getSenderUserName() {
        return senderUserName;
    }

    public String getSenderScreenName() {
        return "@" + senderScreenName;
    }

    public String getSenderProfileImage() {
        return senderProfileImage;
    }

    public String getMessage() {
        return message;
    }
}
