package com.example.filoangler.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Model.MediaItem;
import com.example.filoangler.R;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MediaPagerAdapter extends RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder> {
    private Context context;
    private List<MediaItem> mediaItems;

    // Constructor for direct use with ArrayList<MediaItem>
    public MediaPagerAdapter(Context context, List<MediaItem> mediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
    }

    // Backward compatibility constructor
    public MediaPagerAdapter(Context context, ArrayList<String> mediaUrls, ArrayList<Boolean> isVideoFlags) {
        this.context = context;
        this.mediaItems = new ArrayList<>();
        for (int i = 0; i < mediaUrls.size(); i++) {
            this.mediaItems.add(new MediaItem(mediaUrls.get(i), isVideoFlags.get(i)));
        }
    }

    @NonNull
    @Override
    public MediaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post_media, parent, false);
        return new MediaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MediaViewHolder holder, int position) {
        MediaItem mediaItem = mediaItems.get(position);
        String mediaUrl = mediaItem.getUrl();
        boolean isVideo = mediaItem.isVideo();

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
        return mediaItems.size();
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