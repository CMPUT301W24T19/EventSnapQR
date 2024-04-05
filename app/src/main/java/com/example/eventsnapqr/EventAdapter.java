package com.example.eventsnapqr;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import com.bumptech.glide.Glide;

public class EventAdapter extends ArrayAdapter<Event> {

    public EventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.list_events, parent, false);
        }

        ImageView eventImage = itemView.findViewById(R.id.eventImage);
        TextView eventName = itemView.findViewById(R.id.eventName);
        TextView eventOrganizer = itemView.findViewById(R.id.eventOrganizer);

        Event event = getItem(position);
        eventName.setText(event.getEventName());
        eventOrganizer.setText(event.getOrganizer().getName());

        // Load image using Glide
        Glide.with(getContext())
                .load(event.getPosterURI())
                .placeholder(R.drawable.place_holder_img)
                .into(eventImage);

        return itemView;
    }
}

