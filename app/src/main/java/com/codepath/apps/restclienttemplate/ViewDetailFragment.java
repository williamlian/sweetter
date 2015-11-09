package com.codepath.apps.restclienttemplate;


import android.app.Fragment;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.models.Tweet;
import com.codepath.apps.restclienttemplate.widget.ResizableImageView;
import com.squareup.picasso.Picasso;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ViewDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ViewDetailFragment extends DialogFragment {
    private static final String ARG_TWEET = "tweet";
    private Tweet tweet;
    private RelativeLayout replayLayout;

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

        replayLayout = (RelativeLayout)view.findViewById(R.id.rl_reply);

        tv_userName.setText(tweet.getUserName());
        tv_screenName.setText(tweet.getScreenName());
        Picasso.with(view.getContext()).load(tweet.getBiggerProfileImage()).into(iv_userProfile);
        tv_tweet.setText(tweet.getBody());
        tv_timestamp.setText(tweet.getFormattedTimestamp());
        tv_popularity.setText(tweet.getPopularity());

        iv_reply.setOnClickListener(onReply);

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
    }

    private View.OnClickListener onReply = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            replayLayout.setVisibility(View.VISIBLE);
        }
    };
}
