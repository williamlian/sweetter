package com.codepath.apps.restclienttemplate.adaptor;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.codepath.apps.restclienttemplate.ProfileActivity;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.util.TweetAction;
import com.codepath.apps.restclienttemplate.widget.ResizableImageView;
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
        ImageView iv_retweetIcon;
        ImageView iv_favoriteIcon;
        LinearLayout ll_media;
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
            viewHolder.iv_retweetIcon = (ImageView)convertView.findViewById(R.id.iv_retweetIcon);
            viewHolder.iv_favoriteIcon = (ImageView)convertView.findViewById(R.id.iv_favoriteIcon);
            viewHolder.ll_media = (LinearLayout)convertView.findViewById(R.id.ll_media);
            convertView.setTag(viewHolder);
        }

        Tweet tweet = getItem(position);
        viewHolder.tv_userName.setText(tweet.getUserName());
        viewHolder.tv_screenName.setText(tweet.getScreenName());
        viewHolder.tv_body.setText(tweet.getBody());
        viewHolder.tv_age.setText(tweet.getAge());
        Picasso.with(getContext()).load(tweet.getBiggerProfileImage()).into(viewHolder.iv_userProfile);

        viewHolder.iv_userProfile.setOnClickListener(new OnUserProfileClickHandler(tweet.getScreenName()));

        if(tweet.getMedia() != null) {
            viewHolder.ll_media.removeAllViews();
            for(String url : tweet.getMedia()) {
                ResizableImageView iv_medium = new ResizableImageView(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                iv_medium.setLayoutParams(params);
                viewHolder.ll_media.addView(iv_medium);
                Picasso.with(getContext()).load(url).into(iv_medium);
            }
            viewHolder.ll_media.setVisibility(View.VISIBLE);
        } else {
            viewHolder.ll_media.setVisibility(View.GONE);
        }
        updateTweetStatus(viewHolder, tweet);

        return convertView;
    }

    private void updateTweetStatus(ViewHolder viewHolder, Tweet tweet) {
        viewHolder.tv_retweet.setText(tweet.getRetweetCount().toString());
        viewHolder.tv_favorite.setText(tweet.getFavoriteCount().toString());
        if(tweet.isRetweeted()) {
            viewHolder.iv_retweetIcon.setImageResource(R.drawable.ic_retweeted);
            viewHolder.iv_retweetIcon.setOnClickListener(null);
        } else {
            viewHolder.iv_retweetIcon.setImageResource(R.drawable.ic_retweet);
            viewHolder.iv_retweetIcon.setOnClickListener(new TweetActionHandler(viewHolder, tweet));
        }
        if(tweet.isFavorited()) {
            viewHolder.iv_favoriteIcon.setImageResource(R.drawable.ic_favorited);
            viewHolder.iv_favoriteIcon.setOnClickListener(null);
        } else {
            viewHolder.iv_favoriteIcon.setImageResource(R.drawable.ic_favorite);
            viewHolder.iv_favoriteIcon.setOnClickListener(new TweetActionHandler(viewHolder, tweet));
        }
    }

    /* *********************************************************************************************
     *
     * Tweet Actions
     *
     * *********************************************************************************************/
    private class TweetActionHandler implements View.OnClickListener, TweetAction.TweetActionCallback {
        private ViewHolder viewHolder;
        private Tweet tweet;

        public TweetActionHandler(ViewHolder viewHolder, Tweet tweet) {
            this.viewHolder = viewHolder;
            this.tweet = tweet;
        }

        @Override
        public void onClick(View v) {
            if(v.getId() == R.id.iv_retweetIcon) {
                TweetAction action = new TweetAction(getContext());
                action.retweet(tweet,this);
            } else if(v.getId() == R.id.iv_favoriteIcon) {
                TweetAction action = new TweetAction(getContext());
                action.favorite(tweet, this);
            }
        }

        @Override
        public void onReply(Tweet tweet) { }

        @Override
        public void onRetweet(Tweet tweet) {
            updateTweetStatus(viewHolder, tweet);
        }

        @Override
        public void onFavorite(Tweet tweet) {
            updateTweetStatus(viewHolder, tweet);
        }

        @Override
        public void onTweetActionFailure(int actionType, int status, String error) {
            Toast.makeText(getContext(),"Failed to act on status: " + error, Toast.LENGTH_SHORT).show();
        }
    }

    /* *********************************************************************************************
     *
     * Profile Actions
     *
     * *********************************************************************************************/
    class OnUserProfileClickHandler implements View.OnClickListener {
        private String screenName;
        public OnUserProfileClickHandler(String screenName) {
            this.screenName = screenName;
        }
        @Override
        public void onClick(View v) {
            Intent showProfileIntent = new Intent(getContext(), ProfileActivity.class);
            showProfileIntent.putExtra(ProfileActivity.ARGS_USER, screenName);
            TweetAdaptor.this.getContext().startActivity(showProfileIntent);
        }
    };

}
