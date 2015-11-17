package com.codepath.apps.restclienttemplate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codepath.apps.restclienttemplate.adaptor.UserAdaptor;
import com.codepath.apps.restclienttemplate.client.TwitterClient;
import com.codepath.apps.restclienttemplate.fragment.TimelineFragment;
import com.codepath.apps.restclienttemplate.models.User;
import com.codepath.apps.restclienttemplate.util.AppUtil;
import com.codepath.apps.restclienttemplate.widget.EndlessScrollListener;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ShowFollowActivity extends AppCompatActivity
    implements TimelineFragment.OnTimelineActionHandler
{
    public static final String ARG_USER_LIST_TYPE = "userListType";
    public static final String ARG_SCREEN_NAME = "screenName";

    public static final int FOLLOWERS = 0;
    public static final int FOLLOWINGS = 1;

    static final int FETCH_SIZE = 100;
    static final int HTTP_TOO_MANY_REQUESTS = 429;

    int userListType;
    String screenName;
    ListView lv_users;
    UserAdaptor userAdaptor;
    boolean noMoreUsers = false;
    String cursor = null;
    MenuItem mi_progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_follow);

        lv_users = (ListView)findViewById(R.id.lv_users);
        userAdaptor = new UserAdaptor(this, new ArrayList<User>());

        lv_users.setOnScrollListener(onLoadMoreListener);
        lv_users.setOnItemClickListener(onUserClickListener);

        userListType = getIntent().getExtras().getInt(ARG_USER_LIST_TYPE);
        screenName = getIntent().getExtras().getString(ARG_SCREEN_NAME);

        lv_users.setAdapter(userAdaptor);
        getUserList();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.users, menu);
        mi_progressBar = menu.findItem(R.id.mi_progressBar);
        return super.onCreateOptionsMenu(menu);
    }
    /* *********************************************************************************************
     *
     * Timeline action handlers
     *
     * *********************************************************************************************/
    @Override
    public void onTimelineLoadStart() {
        if(mi_progressBar != null) {
            mi_progressBar.setVisible(true);
        }
    }

    @Override
    public void onTimelineLoadCompleted() {
        if(mi_progressBar != null) {
            mi_progressBar.setVisible(false);
        }
    }

    /* *********************************************************************************************
             *
             * UI Method Triggering Reloads
             *
             * *********************************************************************************************/
    private EndlessScrollListener onLoadMoreListener = new EndlessScrollListener() {
        @Override
        public boolean onLoadMore (int page, int totalItemsCount) {
            if (userAdaptor.getCount() == 0) {
                getUserList();
            } else {
                if(!noMoreUsers) {
                    getUserList();
                } else {
                    return  false;
                }
            }
            return true;
        }
    };

    private AdapterView.OnItemClickListener onUserClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

        }
    };

    /* *********************************************************************************************
     *
     * Network Requests
     *
     * *********************************************************************************************/
    private void getUserList() {
        TwitterClient client = Sweeter.getRestClient();
        if(userListType == FOLLOWERS) {
            client.getFollowers(screenName, FETCH_SIZE, cursor, new UserListResponseHandler(cursor));
        } else if(userListType == FOLLOWINGS){
            client.getFollowings(screenName, FETCH_SIZE, cursor, new UserListResponseHandler(cursor));
        } else {
            Log.e(this.getClass().getName(), "Unknown type " + userListType);
        }
    }

    /* *********************************************************************************************
     *
     * Response Handlers
     *
     * *********************************************************************************************/
    private class UserListResponseHandler extends JsonHttpResponseHandler {
        String requestedCursor;

        public UserListResponseHandler(String cursor) {
            requestedCursor = cursor;
        }
        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
            cursor = response.optString("next_cursor_str");
            List<User> users = User.getUsers(response.optJSONArray("users"));
            if (requestedCursor == null) {
                noMoreUsers = false;
                userAdaptor.clear();
            }
            if(cursor.equals(requestedCursor)) {
                noMoreUsers = true;
            } else {
                userAdaptor.addAll(users);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            if(statusCode == 0) {
                Toast.makeText(ShowFollowActivity.this, "No network available, please load later", Toast.LENGTH_LONG).show();
            } else if(statusCode == HTTP_TOO_MANY_REQUESTS) {
                Toast.makeText(ShowFollowActivity.this, "API rate limit exceeded, please wait some time then refresh", Toast.LENGTH_SHORT).show();
                noMoreUsers = true;
            } else {
                showError(AppUtil.parseJsonError(errorResponse));
                Log.e(this.getClass().getName(), String.format("Rest call failed [%d] %s", statusCode, errorResponse.toString()));
            }
        }
    }

    private void showError(String message) {
        new MaterialDialog.Builder(this)
                .title("Failed to load Timeline")
                .content(message)
                .positiveText("Ok")
                .show();
    }
}
