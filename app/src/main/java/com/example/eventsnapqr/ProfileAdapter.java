package com.example.eventsnapqr;

import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;
public class ProfileAdapter extends RecyclerView.Adapter<ProfileAdapter.UserViewHolder> {
    private List<User> userList;
    private OnClickListener onClickListener;

    public ProfileAdapter(List<User> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_content, parent, false);
        return new UserViewHolder(view);
    }


    public void setOnClickListener(OnClickListener onClickListener) {
        this.onClickListener = onClickListener;
    }

    public interface OnClickListener {
        void onClick(int position, User user);
    }
    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        if (user.getProfilePicture() == null || user.getProfilePicture().isEmpty()){
            Bitmap initialsImageBitmap = user.generateInitialsImage(user.getName());
            Glide.with(holder.itemView.getContext())
                    .load(initialsImageBitmap)
                    .placeholder(R.drawable.place_holder_img)
                    .error(R.drawable.place_holder_img)
                    .into(holder.profilePicture);
        }
        else {
            Glide.with(holder.itemView.getContext())
                    .load(user.getProfilePicture())
                    .placeholder(R.drawable.place_holder_img)
                    .error(R.drawable.place_holder_img)
                    .into(holder.profilePicture);
        }
        holder.userName.setText(user.getName());

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onClickListener != null) {
                    onClickListener.onClick(position, user);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePicture;
        TextView userName;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePicture = itemView.findViewById(R.id.image_pofile_picture);
            userName = itemView.findViewById(R.id.text_user_name);
        }
    }
}