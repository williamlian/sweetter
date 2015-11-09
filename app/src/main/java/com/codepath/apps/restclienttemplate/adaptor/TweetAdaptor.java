package com.codepath.apps.restclienttemplate.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TweetAdaptor extends ArrayAdapter<Tweet> {

    static class ViewHolder {
        ImageView iv_userProfile;
        TextView tv_userName;
        TextView tv_screenName;
        TextView tv_body;
        TextView tv_retweet;
        TextView tv_favorite;
        TextView tv_age;
    }

    public TweetAdaptor(Context context, List<Tweet> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_tweet, parent, false);
        }
        ViewHolder viewHolder = (ViewHolder)convertView.getTag();
        if(viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.iv_userProfile = (ImageView)convertView.findViewById(R.id.iv_userProfile);
            viewHolder.tv_userName = (TextView)convertView.findViewById(R.id.tv_userName);
            viewHolder.tv_screenName = (TextView)convertView.findViewById(R.id.tv_screenName);
            viewHolder.tv_body = (TextView)convertView.findViewById(R.id.tv_body);
            viewHolder.tv_retweet = (TextView)convertView.findViewById(R.id.tv_retweet);
            viewHolder.tv_favorite = (TextView)convertView.findViewById(R.id.tv_favorite);
            viewHolder.tv_age = (TextView)convertView.findViewById(R.id.tv_age);
        }

        Tweet tweet = getItem(position);
        viewHolder.tv_userName.setText(tweet.getUserName());
        viewHolder.tv_screenName.setText(tweet.getScreenName());
        viewHolder.tv_retweet.setText(tweet.getRetweetCount().toString());
        viewHolder.tv_favorite.setText(tweet.getFavoriteCount().toString());
        viewHolder.tv_body.setText(tweet.getBody());
        viewHolder.tv_age.setText(tweet.getAge());
        Picasso.with(getContext()).load(tweet.getBiggerProfileImage()).into(viewHolder.iv_userProfile);

        return convertView;
    }
}
