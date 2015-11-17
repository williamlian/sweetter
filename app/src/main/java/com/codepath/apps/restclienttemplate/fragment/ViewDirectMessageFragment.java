package com.codepath.apps.restclienttemplate.fragment;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.Sweeter;
import com.codepath.apps.restclienttemplate.adaptor.DirectMessageAdaptor;
import com.codepath.apps.restclienttemplate.client.TwitterClient;
import com.codepath.apps.restclienttemplate.models.DirectMessage;
import com.codepath.apps.restclienttemplate.util.AppUtil;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ViewDirectMessageFragment extends DialogFragment {
    public static final int FETCH_SIZE = 100;

    private DirectMessageFragementActionListener mListener;
    private DirectMessageAdaptor directMessageAdaptor;
    private boolean noMoreMessages = false;

    public static ViewDirectMessageFragment newInstance() {
        ViewDirectMessageFragment fragment = new ViewDirectMessageFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public ViewDirectMessageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dm_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ListView ll_dm = (ListView)view.findViewById(R.id.ll_dm);
        directMessageAdaptor = new DirectMessageAdaptor(getActivity(), new ArrayList());
        ll_dm.setAdapter(directMessageAdaptor);

        getDirectMessages(null);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (DirectMessageFragementActionListener) activity;
        } catch (ClassCastException e) {

        }
    }

    public void getDirectMessages(String maxId) {
        TwitterClient client = Sweeter.getRestClient();
        Log.i(getClass().getName(), "get direct messages for logged in user");
        client.getDirectMessages(FETCH_SIZE, maxId, new DirectMessageLoadHandler(maxId));
    }

    class DirectMessageLoadHandler extends JsonHttpResponseHandler {
        String maxId;
        public DirectMessageLoadHandler(String maxId) {
            this.maxId = maxId;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
            List<DirectMessage> directMessages = DirectMessage.getList(response);
            if(directMessages.size() == 0) {
                showError("No Direct Messages");
                dismiss();
                return;
            }
            Log.i(getClass().getName(), String.format("%d messages loaded",directMessages.size()));
            if(maxId == null) {
                noMoreMessages = false;
                directMessageAdaptor.clear();
            }
            int prevLength = directMessageAdaptor.getCount();
            for(DirectMessage dm : directMessages) {
                if(!dm.getId().equals(maxId)) {
                    directMessageAdaptor.add(dm);
                }
            }
            if(prevLength == directMessageAdaptor.getCount()) {
                noMoreMessages = true;
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
            showError(AppUtil.parseJsonError(errorResponse));
        }
    }

    private void showError(String message) {
        new MaterialDialog.Builder(getActivity())
                .title("Failed to Load Messages")
                .content(message)
                .positiveText("Ok")
                .show();
    }

    public interface DirectMessageFragementActionListener {
        void onAction();
    }

}
