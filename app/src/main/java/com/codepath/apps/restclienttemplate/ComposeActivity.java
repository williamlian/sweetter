package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.codepath.apps.restclienttemplate.models.LoginUser;
import com.codepath.apps.restclienttemplate.models.Tweet;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

public class ComposeActivity extends AppCompatActivity {
    TextView tv_lettersLeft;
    EditText et_tweet;

    private static final Integer MAX_LETTER = 140;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        tv_lettersLeft = (TextView) toolbar.findViewById(R.id.tv_letterLeft);
        et_tweet = (EditText)findViewById(R.id.et_tweet);

        TextView tv_userName = (TextView)findViewById(R.id.tv_composerName);
        TextView tv_screenName = (TextView)findViewById(R.id.tv_composerScreenName);
        ImageView iv_userProfile = (ImageView)findViewById(R.id.iv_composerImage);
        Button btn_tweet = (Button)findViewById(R.id.btn_tweet);

        LoginUser loginUser = LoginUser.get();
        tv_userName.setText(loginUser.getUserName());
        tv_screenName.setText(loginUser.getScreenName());

        Picasso.with(this).load(loginUser.getProfileImage()).into(iv_userProfile);
        tv_lettersLeft.setText(MAX_LETTER.toString());
        btn_tweet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postTweet();
            }
        });

        et_tweet.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                Integer typedLength = et_tweet.getText().length();
                tv_lettersLeft.setText(String.valueOf(MAX_LETTER - typedLength));
            }
        });
    }

    public void postTweet() {
        String text = et_tweet.getText().toString();
        if(text == null || text.length() == 0) {
            tweetFail("You have not written anything yet ;-)");
            return;
        }
        RestClient client = RestApplication.getRestClient();
        client.postTweet(text, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Tweet newTweet = new Tweet(response);
                newTweet.save();
                tweetSuccess();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if(statusCode == 0) {
                    tweetFail("Sorry we cannot post your tweet due to network connectivity, please try again later." );
                } else {
                    Log.e(this.getClass().getName(), String.format("Failed to post tweet. [%d] %s", statusCode, errorResponse.toString()));
                    tweetFail("Sorry we cannot post your tweet, please try again later. Error code " + String.valueOf(statusCode));
                }
            }
        });
    }

    public void tweetSuccess() {
        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(R.string.post_success)
                .positiveText("Ok")
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(MaterialDialog materialDialog, DialogAction dialogAction) {
                        setResult(RESULT_OK);
                        ComposeActivity.this.finish();
                    }
                })
                .show();
    }

    public void tweetFail(String message) {
        new MaterialDialog.Builder(this)
                .title(R.string.app_name)
                .content(message)
                .positiveText("Ok")
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.compose, menu);

        return super.onCreateOptionsMenu(menu);
    }

}
