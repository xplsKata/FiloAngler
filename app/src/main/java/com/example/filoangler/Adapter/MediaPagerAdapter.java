package com.example.filoangler.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Model.MediaItem;
import com.example.filoangler.R;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.material.imageview.ShapeableImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class MediaPagerAdapter extends RecyclerView.Adapter<MediaPagerAdapter.MediaViewHolder> {
    private Context context;
    private List<MediaItem> mediaItems;
    private RecyclerView recyclerView;

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

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;

        // Add scroll listener to pause videos when scrolling
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    // Pause all video players when scrolling starts
                    pauseAllVideos();
                }
            }
        });
    }

    // Method to pause all video players
    private void pauseAllVideos() {
        for (int i = 0; i < getItemCount(); i++) {
            MediaViewHolder holder = (MediaViewHolder) recyclerView.findViewHolderForAdapterPosition(i);
            if (holder != null && holder.player != null && holder.player.isPlaying()) {
                holder.player.pause();

                // Update play/pause button if it exists
                if (holder.playPauseButton != null) {
                    holder.playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                }
            }
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
            holder.videoContainer.setVisibility(View.VISIBLE);

            // Initialize ExoPlayer if not already initialized
            if (holder.player == null) {
                holder.player = new SimpleExoPlayer.Builder(context).build();
                holder.videoView.setPlayer(holder.player);
            }

            // Prepare media item
            com.google.android.exoplayer2.MediaItem videoItem = com.google.android.exoplayer2.MediaItem.fromUri(mediaUrl);
            holder.player.setMediaItem(videoItem);
            holder.player.prepare();

            holder.player.addListener(new Player.Listener() {
                @Override
                public void onRenderedFirstFrame() {
                    // Similar to your onPrepared logic, but for ExoPlayer
                    int videoWidth = holder.player.getVideoSize().width;
                    int videoHeight = holder.player.getVideoSize().height;

                    holder.videoContainer.post(() -> {
                        int containerWidth = holder.videoContainer.getWidth();
                        int containerHeight = holder.videoContainer.getHeight();

                        float videoRatio = (float) videoWidth / videoHeight;
                        float containerRatio = (float) containerWidth / containerHeight;

                        int finalWidth;
                        int finalHeight;

                        if (videoRatio > containerRatio) {
                            finalWidth = containerWidth;
                            finalHeight = (int) (containerWidth / videoRatio);
                        } else {
                            finalHeight = containerHeight;
                            finalWidth = (int) (containerHeight * videoRatio);
                        }

                        ViewGroup.LayoutParams params = holder.videoView.getLayoutParams();
                        params.width = finalWidth;
                        params.height = finalHeight;
                        holder.videoView.setLayoutParams(params);
                    });
                }
            });

            // Set up play/pause functionality
            holder.playPauseButton.setVisibility(View.VISIBLE);
            holder.player.setRepeatMode(Player.REPEAT_MODE_ONE);
            holder.player.setPlayWhenReady(false);

            // Play/Pause click listener
            holder.playPauseButton.setOnClickListener(v -> {
                if (holder.player.isPlaying()) {
                    holder.player.pause();
                    holder.playPauseButton.setImageResource(android.R.drawable.ic_media_play);
                } else {
                    holder.player.play();
                    holder.playPauseButton.setImageDrawable(null);
                }
            });

            // Optional: Also handle clicks on the video view itself
            holder.videoView.setOnClickListener(v -> {
                holder.playPauseButton.performClick();
            });

        } else {
            // Image handling remains the same
            holder.videoContainer.setVisibility(View.GONE);
            holder.videoView.setVisibility(View.GONE);
            holder.playPauseButton.setVisibility(View.GONE);
            holder.imageView.setVisibility(View.VISIBLE);
            Picasso.get()
                    .load(mediaUrl)
                    .fit()
                    .centerCrop()
                    .into(holder.imageView);
        }
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    @Override
    public void onViewRecycled(@NonNull MediaViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.player != null) {
            holder.player.release();
            holder.player = null;
        }
    }

    public class MediaViewHolder extends RecyclerView.ViewHolder {
        ShapeableImageView imageView;
        PlayerView videoView;
        FrameLayout videoContainer;
        ImageButton playPauseButton;
        SimpleExoPlayer player;

        public MediaViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.postImage);
            videoView = itemView.findViewById(R.id.postVideo);
            videoContainer = itemView.findViewById(R.id.postVideoContainer);
            playPauseButton = itemView.findViewById(R.id.playPauseButton);
        }
    }
}