package com.codepath.apps.restclienttemplate.fragment;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.util.TweetAction;
import com.codepath.apps.restclienttemplate.widget.ResizableImageView;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewDetailFragment extends DialogFragment implements TweetAction.TweetActionCallback {
    private static final String ARG_TWEET = "tweet";
    private Tweet tweet;
    private RelativeLayout replyLayout;

    public static ViewDetailFragment newInstance(Tweet tweet) {
        ViewDetailFragment fragment = new ViewDetailFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_TWEET, tweet);
        fragment.setArguments(args);
        return fragment;
    }

    public ViewDetailFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            tweet = (Tweet)getArguments().getSerializable(ARG_TWEET);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_view_detail, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

//        int width = getResources().getDisplayMetrics().widthPixels;
//        int height = getResources().getDisplayMetrics().heightPixels;
//        getDialog().getWindow().setLayout(width , height);

        TextView tv_userName = (TextView)view.findViewById(R.id.tv_userName);
        TextView tv_screenName = (TextView)view.findViewById(R.id.tv_userScreenName);
        ImageView iv_userProfile = (ImageView)view.findViewById(R.id.iv_userImage);
        TextView tv_tweet = (TextView)view.findViewById(R.id.tv_tweet);
        TextView tv_timestamp = (TextView)view.findViewById(R.id.tv_timestamp);
        TextView tv_popularity = (TextView)view.findViewById(R.id.tv_popularity);
        LinearLayout ll_media = (LinearLayout)view.findViewById(R.id.ll_media);
        ImageView iv_reply = (ImageView)view.findViewById(R.id.iv_reply);

        replyLayout = (RelativeLayout)view.findViewById(R.id.rl_reply);
        replyLayout.setVisibility(View.GONE);

        tv_userName.setText(tweet.getUserName());
        tv_screenName.setText(tweet.getScreenName());
        Picasso.with(view.getContext()).load(tweet.getBiggerProfileImage()).into(iv_userProfile);
        tv_tweet.setText(tweet.getBody());
        tv_timestamp.setText(tweet.getFormattedTimestamp());
        tv_popularity.setText(tweet.getPopularity());

        iv_reply.setOnClickListener(onReplyIconClicked);

        if(tweet.getMedia() != null) {
            for(String url : tweet.getMedia()) {
                ResizableImageView iv_medium = new ResizableImageView(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                iv_medium.setLayoutParams(params);
                ll_media.addView(iv_medium);
                Picasso.with(getContext()).load(url).into(iv_medium);
            }
            ll_media.setVisibility(View.VISIBLE);
        } else {
            ll_media.setVisibility(View.GONE);
        }

        updateTweetStatus();
    }

    private void updateTweetStatus() {
        ImageView iv_retweetIcon = (ImageView)this.getView().findViewById(R.id.iv_retweet);
        ImageView iv_favoriteIcon = (ImageView)this.getView().findViewById(R.id.iv_favorite);

        if(tweet.isRetweeted()) {
            iv_retweetIcon.setImageResource(R.drawable.ic_retweeted);
            iv_retweetIcon.setOnClickListener(null);
        } else {
            iv_retweetIcon.setImageResource(R.drawable.ic_retweet);
            iv_retweetIcon.setOnClickListener(onRetweetIconClicked);
        }
        if(tweet.isFavorited()) {
            iv_favoriteIcon.setImageResource(R.drawable.ic_favorited);
            iv_favoriteIcon.setOnClickListener(null);
        } else {
            iv_favoriteIcon.setImageResource(R.drawable.ic_favorite);
            iv_favoriteIcon.setOnClickListener(onFavoriteIconClicked);
        }
    }

    private View.OnClickListener onReplyIconClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            replyLayout.setVisibility(View.VISIBLE);
            Button replyButton = (Button)replyLayout.findViewById(R.id.btn_reply);
            final EditText et_reply = (EditText)replyLayout.findViewById(R.id.et_reply);
            replyButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String replyText = et_reply.getText().toString();
                    if(replyText.length() == 0) {
                        showError("You don't type anything to reply :-)");
                    } else {
                        TweetAction action = new TweetAction(getContext());
                        action.reply(tweet, replyText, ViewDetailFragment.this);
                    }
                }
            });
        }
    };

    private View.OnClickListener onRetweetIconClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TweetAction action = new TweetAction(getContext());
            action.retweet(tweet, ViewDetailFragment.this);
        }
    };

    private View.OnClickListener onFavoriteIconClicked = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            TweetAction action = new TweetAction(getContext());
            action.favorite(tweet, ViewDetailFragment.this);
        }
    };

    private void showError(String message) {
        new MaterialDialog.Builder(getContext())
                .title("Failed to load Timeline")
                .content(message)
                .positiveText("Ok")
                .show();
    }


  /* *********************************************************************************************
   *
   * Tweet Action Callbacks
   *
   * *********************************************************************************************/
    @Override
    public void onReply(Tweet tweet) {
        replyLayout.setVisibility(View.GONE);
        //this.tweet = tweet;
        Toast.makeText(getContext(),"Your reply has been posted", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRetweet(Tweet tweet) {
        Toast.makeText(getContext(),"You have retweeted this status", Toast.LENGTH_SHORT).show();
        this.tweet = tweet;
        updateTweetStatus();
    }

    @Override
    public void onFavorite(Tweet tweet) {
        Toast.makeText(getContext(),"You likes this status", Toast.LENGTH_SHORT).show();
        this.tweet = tweet;
        updateTweetStatus();
    }

    @Override
    public void onTweetActionFailure(int actionType, int status, String error) {
        showError(error);
    }
}
