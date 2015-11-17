package com.codepath.apps.restclienttemplate.models;

import com.activeandroid.Model;
import com.activeandroid.annotation.Column;
import com.activeandroid.annotation.Table;
import com.activeandroid.query.Delete;
import com.activeandroid.query.Select;
import com.codepath.apps.restclienttemplate.util.DateUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ocpsoft.prettytime.Duration;
import org.ocpsoft.prettytime.PrettyTime;
import org.ocpsoft.prettytime.TimeUnit;
import org.ocpsoft.prettytime.units.Day;
import org.ocpsoft.prettytime.units.Hour;
import org.ocpsoft.prettytime.units.Minute;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Table(name = "Tweets")
public class Tweet extends Model implements Serializable {
    public static final String SOURCE_TIMELINE = "timeline";
    public static final String SOURCE_MENTION = "mention";
    public static final String SOURCE_USER = "user";
    public static final String SOURCE_SEARCH = "search";

    // Define database columns and associated fields
    @Column(name = "tweetId")
    String tweetId;
    @Column(name = "userId")
    int userId;
    @Column(name = "userName")
    String userName;
    @Column(name = "screenName")
    String screenName;
    @Column(name = "profileImage")
    String profileImage;
    @Column(name = "retweetCount")
    int retweetCount;
    @Column(name = "favoriteCount")
    int favoriteCount;
    @Column(name = "retweeted")
    Boolean retweeted;
    @Column(name = "favorited")
    Boolean favorited;
    @Column(name = "timestamp")
    Date timestamp;
    @Column(name = "body")
    String body;
    @Column(name = "source")
    String source;

    List<String> media;

    // Make sure to always define this constructor with no arguments
    public Tweet() {
        super();
    }

    public Tweet(JSONObject object, String source) {
        super();
        update(object);
    }

    public void update(JSONObject object) {
        try {
            JSONObject user = object.getJSONObject("user");
            this.tweetId = object.getString("id_str");
            this.userId = user.getInt("id");
            this.userName = user.getString("name");
            this.screenName = user.getString("screen_name");
            this.profileImage = user.getString("profile_image_url");
            this.retweetCount = object.getInt("retweet_count");
            this.favoriteCount = object.getInt("favorite_count");
            this.retweeted = object.getBoolean("retweeted");
            this.favorited = object.getBoolean("favorited");
            this.timestamp = DateUtil.convertStringToDate(object.getString("created_at"));
            this.body = object.optString("text");

            JSONArray media = object.getJSONObject("entities").optJSONArray("media");
            if(media != null) {
                for(int i = 0; i < media.length(); i++) {
                    JSONObject medium = media.getJSONObject(i);
                    if("photo".equals(medium.optString("type"))) {
                        if(this.media == null) {
                            this.media = new ArrayList<>();
                        }
                        this.media.add(medium.getString("media_url"));
                    }
                }
            } else {
                this.media = null;
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public static void clearLocal(String source) {
        new Delete().from(Tweet.class).where("source=?",source).execute();
    }

    public static ArrayList<Tweet> fromJson(JSONArray jsonArray, String source) {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tweetJson = null;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            Tweet tweet = new Tweet(tweetJson, source);
            tweet.save();
            tweets.add(tweet);
        }
        return tweets;
    }


    public static List<Tweet> fromLocal(String source) {
        return new Select().from(Tweet.class).where("source=?",source).orderBy("timestamp DESC").execute();
    }


    public String getUserName() {
        return userName;
    }

    public String getBody() {
        return body;
    }

    public String getScreenName() {
        return "@" + screenName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public String getBiggerProfileImage() {
        return getProfileImage().replace("_normal","_bigger");
    }

    public Integer getRetweetCount() {
        return retweetCount;
    }

    public Integer getFavoriteCount() {
        return favoriteCount;
    }

    public boolean isRetweeted() {
        return retweeted;
    }

    public boolean isFavorited() {
        return favorited;
    }

    public String getTweetId() {
        return tweetId;
    }

    public String getAge() {
        PrettyTime p = new PrettyTime();
        Duration d = p.approximateDuration(this.timestamp);
        TimeUnit unit = d.getUnit();
        Long q = - d.getQuantity();
        String suffix;
        if(unit.getMillisPerUnit() < new Minute().getMillisPerUnit()) {
            return "Now";
        } else if(unit.getMillisPerUnit() == new Minute().getMillisPerUnit()) {
            suffix = "m";
        } else if(unit.getMillisPerUnit() == new Hour().getMillisPerUnit()) {
            suffix = "h";
        } else if(unit.getMillisPerUnit() == new Day().getMillisPerUnit()) {
            suffix = "d";
        } else {
            return p.format(this.timestamp);
        }
        return q.toString() + suffix;
    }

    public String getFormattedTimestamp() {
        SimpleDateFormat format = new SimpleDateFormat("M/d/yyyy K:m a");
        return format.format(timestamp);
    }

    public String getPopularity() {
        return String.format("%d RETWEETS %d LIKES", retweetCount, favoriteCount);
    }

    public List<String> getMedia() {
        return media;
    }

    public void retweet() {
        this.retweeted = true;
        this.retweetCount++;
    }

    public void favorite() {
        this.favorited = true;
        this.favoriteCount++;
    }

    public void unfavorite() {
        this.favorited = false;
        this.favoriteCount--;
    }
}