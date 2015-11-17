package com.codepath.apps.restclienttemplate.fragment;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.Sweeter;
import com.codepath.apps.restclienttemplate.client.TwitterClient;
import com.codepath.apps.restclienttemplate.models.User;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.squareup.picasso.Picasso;

import org.apache.http.Header;
import org.json.JSONObject;

public class SendDirectMessageFragment extends DialogFragment {
    public static final String ARG_RECIPIENT = "recipient";

    public SendDirectMessageFragment() {}

    private User recipient;
    private EditText et_message;

    public static SendDirectMessageFragment newInstance(User recipient) {
        SendDirectMessageFragment fragment = new SendDirectMessageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RECIPIENT, recipient);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        recipient = (User)getArguments().getSerializable(ARG_RECIPIENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dm_send, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        TextView tv_userName = (TextView)view.findViewById(R.id.tv_composerName);
        TextView tv_screenName = (TextView)view.findViewById(R.id.tv_composerScreenName);
        ImageView iv_userProfile = (ImageView)view.findViewById(R.id.iv_composerImage);
        Button btn_send = (Button)view.findViewById(R.id.btn_send);
        et_message = (EditText)view.findViewById(R.id.et_tweet);

        tv_userName.setText(recipient.getUserName());
        tv_screenName.setText(recipient.getScreenName());
        Picasso.with(view.getContext()).load(recipient.getProfileImage()).into(iv_userProfile);
        btn_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send();
            }
        });
    }

    public void send() {
        String text = et_message.getText().toString();
        if(text == null || text.length() == 0) {
            fail("You have not written anything yet ;-)");
            return;
        }
        TwitterClient client = Sweeter.getRestClient();
        client.postDirectMessage(recipient.getScreenName(), text, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                success();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                if (statusCode == 0) {
                    fail("Sorry we cannot post your tweet due to network connectivity, please try again later.");
                } else {
                    Log.e(this.getClass().getName(), String.format("Failed to post tweet. [%d] %s", statusCode, errorResponse.toString()));
                    fail("Sorry we cannot post your tweet, please try again later. Error code " + String.valueOf(statusCode));
                }
            }
        });
    }

    public void success() {
        Toast.makeText(getContext(),"Message Sent", Toast.LENGTH_SHORT).show();
        dismiss();
    }

    public void fail(String message) {
        new MaterialDialog.Builder(this.getContext())
                .title(R.string.app_name)
                .content(message)
                .positiveText("Ok")
                .show();
    }
}
