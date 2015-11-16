package com.codepath.apps.restclienttemplate.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.models.User;
import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdaptor extends ArrayAdapter<User> {

    static class ViewHolder {
        ImageView iv_profile;
        TextView tv_userName;
        TextView tv_screenName;
    }

    private User user;

    public UserAdaptor(Context context, List<User> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        user = getItem(position);
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_user, parent, false);
        }
        ViewHolder viewHolder = (ViewHolder)convertView.getTag();
        if(viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.iv_profile = (ImageView)convertView.findViewById(R.id.iv_userProfile);
            viewHolder.tv_userName = (TextView)convertView.findViewById(R.id.tv_userName);
            viewHolder.tv_screenName = (TextView)convertView.findViewById(R.id.tv_screenName);
            convertView.setTag(viewHolder);
        }

        Picasso.with(getContext()).load(user.getProfileImage()).into(viewHolder.iv_profile);
        viewHolder.tv_userName.setText(user.getUserName());
        viewHolder.tv_screenName.setText(user.getScreenName());

        return convertView;
    }
}
