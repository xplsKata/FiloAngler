package com.example.filoangler.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MediaPagerAdapter extends RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder> {
    private Context context;
    private ArrayList<String> mediaUrls;
    private ArrayList<Boolean> isVideoFlags;

    public MediaPagerAdapter(Context context, ArrayList<String> mediaUrls, ArrayList<Boolean> isVideoFlags) {
        this.context = context;
        this.mediaUrls = mediaUrls;
        this.isVideoFlags = isVideoFlags;
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        String mediaUrl = mediaUrls.get(position);
        boolean isVideo = isVideoFlags.get(position);

        if (isVideo) {
            holder.imageView.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.VISIBLE);

            holder.videoView.setVideoURI(Uri.parse(mediaUrl));
            holder.videoView.setOnPreparedListener(mp -> {
                mp.setLooping(true);
                holder.videoView.start();
            });
        } else {
            holder.videoView.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            Picasso.get().load(mediaUrl).into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return mediaUrls.size();
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imageView;
        VideoView videoView;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.postImage);
            videoView = itemView.findViewById(R.id.postVideo);
        }
    }
}
