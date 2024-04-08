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

/**
 * Adapter for the browse events list, used to populate each of the view inside the layout
 */
public class EventAdapter extends ArrayAdapter<Event> {

    public EventAdapter(Context context, List<Event> events) {
        super(context, 0, events);
    }

    /**
     * Gets the view information used to populate the layout list_events
     * @param position The position of the item within the adapter's data set of the item whose view
     *        we want.
     * @param convertView The old view to reuse, if possible. Note: You should check that this view
     *        is non-null and of an appropriate type before using. If it is not possible to convert
     *        this view to display the correct data, this method can create a new view.
     *        Heterogeneous lists can specify their number of view types, so that this View is
     *        always of the right type (see {@link #getViewTypeCount()} and
     *        {@link #getItemViewType(int)}).
     * @param parent The parent that this view will eventually be attached to
     * @return
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View itemView = convertView;
        if (itemView == null) {
            itemView = LayoutInflater.from(getContext()).inflate(R.layout.list_events, parent, false);
        }

        ImageView eventImage = itemView.findViewById(R.id.eventImage);
        TextView eventName = itemView.findViewById(R.id.eventName);
        TextView eventAddress = itemView.findViewById(R.id.eventAddress);
        TextView eventOrganizer = itemView.findViewById(R.id.eventOrganizer);

        Event event = getItem(position);
        eventName.setText(event.getEventName());
        eventAddress.setText(event.getAddress());
        eventOrganizer.setText(event.getOrganizer().getName());

        Glide.with(getContext())
                .load(event.getPosterURI())
                .placeholder(R.drawable.place_holder_img)
                .into(eventImage);

        return itemView;
    }
}

