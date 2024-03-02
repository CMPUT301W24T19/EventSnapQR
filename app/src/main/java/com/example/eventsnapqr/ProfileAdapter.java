package com.example.eventsnapqr;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.ProfileViewHolder> {

    private List<User> profiles;
    public ProfileAdapter(List<User> profileList) {
        this.profiles = profileList;
    }


    @NonNull
    @Override
    public ProfileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.profile_thumbnail_item, parent, false);
        return new ProfileViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ProfileViewHolder holder, int position) {
        User user = profiles.get(position);

        holder.ivProfile.setImageResource(R.drawable.profile_pic);

    }



    @Override
    public int getItemCount() {
        return (profiles != null) ? profiles.size() : 0;
    }


    public class ProfileViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProfile;
        public ProfileViewHolder(View itemView) {
            super(itemView);
            ivProfile = itemView.findViewById(R.id.iv_profile); // Initialize the ImageView
        }
    }


}
