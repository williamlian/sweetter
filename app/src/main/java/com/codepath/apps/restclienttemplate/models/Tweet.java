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

/*
[
   {
    "created_at": "Sun Nov 08 00:06:36 +0000 2015",
    "id": 663145559314833400,
    "id_str": "663145559314833408",
    "text": "Update: BART system-wide delays have cleared. Regular service has been restored.",
    "source": "<a href="http://www.hootsuite.com" rel="nofollow">Hootsuite</a>",
    "truncated": false,
    "in_reply_to_status_id": null,
    "in_reply_to_status_id_str": null,
    "in_reply_to_user_id": null,
    "in_reply_to_user_id_str": null,
    "in_reply_to_screen_name": null,
    "user":  {
      "id": 55352474,
      "id_str": "55352474",
      "name": "511 Bay Area",
      "screen_name": "511SFBay",
      "location": "San Francisco Bay Area, CA",
      "description": "511 is your free one-stop phone and web source for up-to-the minute Bay Area traffic, transit, rideshare, bicycling, and parking information.",
      "url": "http://t.co/lGyJIpGelm",
      "entities":  {
        "url":  {
          "urls":  [
             {
              "url": "http://t.co/lGyJIpGelm",
              "expanded_url": "http://www.511.org/",
              "display_url": "511.org",
              "indices":  [
                0,
                22
              ]
            }
          ]
        },
        "description":  {
          "urls":  []
        }
      },
      "protected": false,
      "followers_count": 44030,
      "friends_count": 46,
      "listed_count": 1233,
      "created_at": "Thu Jul 09 20:30:59 +0000 2009",
      "favourites_count": 0,
      "utc_offset": -28800,
      "time_zone": "Pacific Time (US & Canada)",
      "geo_enabled": false,
      "verified": false,
      "statuses_count": 24399,
      "lang": "en",
      "contributors_enabled": false,
      "is_translator": false,
      "is_translation_enabled": false,
      "profile_background_color": "FFFFFF",
      "profile_background_image_url": "http://pbs.twimg.com/profile_background_images/163893433/MT29355_511TwitterBkgd_R9.jpg",
      "profile_background_image_url_https": "https://pbs.twimg.com/profile_background_images/163893433/MT29355_511TwitterBkgd_R9.jpg",
      "profile_background_tile": false,
      "profile_image_url": "http://pbs.twimg.com/profile_images/596385508890021888/ZXMtba1G_normal.jpg",
      "profile_image_url_https": "https://pbs.twimg.com/profile_images/596385508890021888/ZXMtba1G_normal.jpg",
      "profile_banner_url": "https://pbs.twimg.com/profile_banners/55352474/1431025059",
      "profile_link_color": "89C9FA",
      "profile_sidebar_border_color": "D3D3D3",
      "profile_sidebar_fill_color": "E3E6E3",
      "profile_text_color": "4D4D4F",
      "profile_use_background_image": true,
      "has_extended_profile": false,
      "default_profile": false,
      "default_profile_image": false,
      "following": true,
      "follow_request_sent": false,
      "notifications": false
    },
    "geo": null,
    "coordinates": null,
    "place": null,
    "contributors": null,
    "is_quote_status": false,
    "retweet_count": 3,
    "favorite_count": 1,
    "entities":  {
      "hashtags":  [],
      "symbols":  [],
      "user_mentions":  [],
      "urls":  []
    },
    "favorited": false,
    "retweeted": false,
    "lang": "en"
  }
]
 */
@Table(name = "Tweets")
public class Tweet extends Model implements Serializable {
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

    List<String> media;

    // Make sure to always define this constructor with no arguments
    public Tweet() {
        super();
    }

    public Tweet(JSONObject object) {
        super();
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

    public static void clearLocal() {
        new Delete().from(Tweet.class).execute();
    }

    public static ArrayList<Tweet> fromJson(JSONArray jsonArray) {
        ArrayList<Tweet> tweets = new ArrayList<Tweet>(jsonArray.length());

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject tweetJson = null;
            try {
                tweetJson = jsonArray.getJSONObject(i);
            } catch (Exception e) {
                e.printStackTrace();
                continue;
            }
            Tweet tweet = new Tweet(tweetJson);
            tweet.save();
            tweets.add(tweet);
        }
        return tweets;
    }


    public static List<Tweet> fromLocal() {
        return new Select().from(Tweet.class).orderBy("timestamp DESC").execute();
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
}