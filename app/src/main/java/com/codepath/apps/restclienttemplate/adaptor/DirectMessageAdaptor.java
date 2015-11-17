package com.codepath.apps.restclienttemplate.adaptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.codepath.apps.restclienttemplate.R;
import com.codepath.apps.restclienttemplate.models.DirectMessage;
import com.squareup.picasso.Picasso;

import java.util.List;

public class DirectMessageAdaptor extends ArrayAdapter {
    static class ViewHolder {
        ImageView iv_userProfile;
        TextView tv_userName;
        TextView tv_screenName;
        TextView tv_body;
    }

    public DirectMessageAdaptor(Context context, List objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if(convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.content_direct_message,parent,false);
        }
        ViewHolder viewHolder = (ViewHolder)convertView.getTag();
        if(viewHolder == null) {
            viewHolder = new ViewHolder();
            viewHolder.iv_userProfile = (ImageView)convertView.findViewById(R.id.iv_userProfile);
            viewHolder.tv_userName = (TextView)convertView.findViewById(R.id.tv_userName);
            viewHolder.tv_screenName = (TextView)convertView.findViewById(R.id.tv_screenName);
            viewHolder.tv_body = (TextView)convertView.findViewById(R.id.tv_body);
            convertView.setTag(viewHolder);
        }

        DirectMessage message = (DirectMessage)getItem(position);

        Picasso.with(getContext()).load(message.getSenderProfileImage()).into(viewHolder.iv_userProfile);
        viewHolder.tv_userName.setText(message.getSenderUserName());
        viewHolder.tv_screenName.setText(message.getSenderScreenName());
        viewHolder.tv_body.setText(message.getMessage());

        return convertView;
    }
}
