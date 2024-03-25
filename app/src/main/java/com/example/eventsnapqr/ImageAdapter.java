package com.example.eventsnapqr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Object> itemList; // List containing both Event and User objects
    private OnClickListener onClickListener;

    public ImageAdapter(List<Object> itemList) {
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.event_poster_item, parent, false);
        return new ImageViewHolder(view);
    }

    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(int position, Object item); // Object type to handle both Event and User objects
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        Object item = itemList.get(position);
        if (item instanceof Event) {
            Event event = (Event) item;
            Glide.with(holder.itemView.getContext())
                    .load(event.getPosterURI())
                    .placeholder(R.drawable.place_holder_img)
                    .error(R.drawable.place_holder_img)
                    .into(holder.ivEventPoster);
        } else if (item instanceof User) {
            User user = (User) item;
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfilePicture())
                    .placeholder(R.drawable.place_holder_img)
                    .error(R.drawable.place_holder_img)
                    .into(holder.ivEventPoster);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(position, item);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    static class ImageViewHolder extends RecyclerView.ViewHolder {
        ImageView ivEventPoster;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            ivEventPoster = itemView.findViewById(R.id.image_pofile_picture);
        }
    }
}
