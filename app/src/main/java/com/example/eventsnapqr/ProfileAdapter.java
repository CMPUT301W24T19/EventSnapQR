package com.example.eventsnapqr;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;


class ProfileAdapter extends ArrayAdapter<User> {

    private LayoutInflater inflater;
    private ArrayList<User> users;
    private Context context;


    public ProfileAdapter(Context context, ArrayList<User> users) {
        super(context, 0, users);
        this.context = context;
        this.users = users;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.profile_thumbnail_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.textViewUserId = convertView.findViewById(R.id.textview_userid);
            viewHolder.textViewUserName = convertView.findViewById(R.id.textview_username);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Get the event at the specified position
        User user = getItem(position);
        if (user != null) {
            viewHolder.textViewUserName.setText(user.getName());
            viewHolder.textViewUserId.setText(user.getDeviceID());
            // Check if organizer is not null before accessing its properties

        }
        return convertView;
    }

    private static class ViewHolder {
        TextView textViewUserName;
        TextView textViewUserId;
        // Add more views if needed
    }
}