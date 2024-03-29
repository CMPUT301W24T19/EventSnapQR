package com.example.eventsnapqr;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class EventArrayAdapter extends ArrayAdapter<Event> {
    private LayoutInflater inflater;
    private ArrayList<Event> events;
    private Context context;
    public EventArrayAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
        inflater = LayoutInflater.from(context);
        this.context = context;
        this.events = events;


    }
    /*
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.item_event, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.eventNameTextView = convertView.findViewById(R.id.textview_event_name);
            viewHolder.qrLinkTextView = convertView.findViewById(R.id.textview_qr_link);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        // Get the event at the specified position
        Event event = getItem(position);
        if (event != null) {
            viewHolder.eventNameTextView.setText(event.getEventName());
            // Check if organizer is not null before accessing its properties
            if (event.getOrganizer() != null) {
                viewHolder.qrLinkTextView.setText(event.getQrCode().getLink());
            } else {
                viewHolder.qrLinkTextView.setText("Unknown link");
            }
        }
        return convertView;
    }

    private static class ViewHolder {
        TextView eventNameTextView;
        TextView qrLinkTextView;
        // Add more views if needed
    }
*/}
