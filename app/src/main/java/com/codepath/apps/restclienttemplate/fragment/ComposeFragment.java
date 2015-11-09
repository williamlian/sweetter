package com.codepath.apps.restclienttemplate.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.RestApplication;
import com.codepath.apps.restclienttemplate.client.TwitterClient;
import com.codepath.apps.restclienttemplate.models.LoginUser;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;


public class ComposeFragment extends DialogFragment {
    TextView tv_lettersLeft;
    EditText et_tweet;

    private static final Integer MAX_LETTER = 140;

    private OnComposeDialogCompleteListenter mListener;


    public static ComposeFragment newInstance() {
        ComposeFragment fragment = new ComposeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ComposeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mListener = (OnComposeDialogCompleteListenter)getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_compose, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tv_lettersLeft = (TextView)view.findViewById(R.id.tv_letterLeft);
        et_tweet = (EditText)view.findViewById(R.id.et_tweet);

        TextView tv_userName = (TextView)view.findViewById(R.id.tv_composerName);
        TextView tv_screenName = (TextView)view.findViewById(R.id.tv_composerScreenName);
        ImageView iv_userProfile = (ImageView)view.findViewById(R.id.iv_composerImage);
        Button btn_tweet = (Button)view.findViewById(R.id.btn_tweet);

        LoginUser loginUser = LoginUser.get();
        tv_userName.setText(loginUser.getUserName());
        tv_screenName.setText(loginUser.getScreenName());

        Picasso.with(view.getContext()).load(loginUser.getProfileImage()).into(iv_userProfile);
        tv_lettersLeft.setText(MAX_LETTER.toString() + "/" + MAX_LETTER.toString());

        et_tweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                Integer typedLength = et_tweet.getText().length();
                tv_lettersLeft.setText(String.valueOf(MAX_LETTER) + "/" + String.valueOf(MAX_LETTER - typedLength));
            }
        });

        btn_tweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postTweet();
            }
        });
    }

    public void postTweet() {
        String text = et_tweet.getText().toString();
        if(text == null || text.length() == 0) {
            tweetFail("You have not written anything yet ;-)");
            return;
        }
        TwitterClient client = RestApplication.getRestClient();
        client.postTweet(text, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Tweet newTweet = new Tweet(response);
                newTweet.save();
                tweetSuccess();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (statusCode == 0) {
                    tweetFail("Sorry we cannot post your tweet due to network connectivity, please try again later.");
                } else {
                    Log.e(this.getClass().getName(), String.format("Failed to post tweet. [%d] %s", statusCode, errorResponse.toString()));
                    tweetFail("Sorry we cannot post your tweet, please try again later. Error code " + String.valueOf(statusCode));
                }
            }
        });
    }

    public void tweetSuccess() {
        new MaterialDialog.Builder(this.getContext())
                .title(R.string.app_name)
                .content(R.string.post_success)
                .positiveText("Ok")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        mListener.onPost();
                        ComposeFragment.this.dismiss();
                    }
                })
                .show();
    }

    public void tweetFail(String message) {
        new MaterialDialog.Builder(this.getContext())
                .title(R.string.app_name)
                .content(message)
                .positiveText("Ok")
                .show();
    }

    public interface OnComposeDialogCompleteListenter {
        void onPost();
    }

}
