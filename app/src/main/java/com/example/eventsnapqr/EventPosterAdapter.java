package com.example.eventsnapqr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
public class EventPosterAdapter extends RecyclerView.Adapter<EventPosterAdapter.EventPosterViewHolder> {
    private List<Event> eventList;

    public EventPosterAdapter(List<Event> eventList) {
        this.eventList = eventList;
    }

    @NonNull
    @Override
    public EventPosterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_poster_item, parent, false);
        return new EventPosterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventPosterViewHolder holder, int position) {
        Event event = eventList.get(position);
        Glide.with(holder.itemView.getContext())
                .load(event.getPosterUri())
                .placeholder(R.drawable.place_holder_img)
                .error(R.drawable.place_holder_img)
                .into(holder.ivEventPoster);

    }


    @Override
    public int getItemCount() {
        return eventList.size();
    }

    static class EventPosterViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventPoster;

        public EventPosterViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventPoster = itemView.findViewById(R.id.iv_event_poster);
        }
    }
}