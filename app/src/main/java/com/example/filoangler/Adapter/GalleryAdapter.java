package com.example.filoangler.Adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.Model.MediaItem;
import com.example.filoangler.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private Context context;
    private ArrayList<MediaItem> mediaItems;
    private ArrayList<MediaItem> selectedMediaItems;
    private GalleryAdapterCallback callback;

    private static final int MAX_SELECTIONS = 10;

    private Uri uriToLoad;

    public GalleryAdapter(Context context, ArrayList<MediaItem> mediaItems, ArrayList<MediaItem> selectedMediaItems) {
        this.context = context;
        this.mediaItems = mediaItems;
        this.selectedMediaItems = selectedMediaItems;
        if (context instanceof GalleryAdapterCallback) {
            this.callback = (GalleryAdapterCallback) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement GalleryAdapterCallback");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_media, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        MediaItem mediaItem = mediaItems.get(position);

        loadThumbnail(holder.imageView, mediaItem);

        // Show video indicator if it's a video
        holder.videoIndicator.setVisibility(mediaItem.isVideo() ? View.VISIBLE : View.GONE);

        int selectionIndex = selectedMediaItems.indexOf(mediaItem);
        updateSelectionUI(holder, selectionIndex);

        holder.imageView.setOnClickListener(v -> {
            handleMediaClick(mediaItem, holder);
        });

        holder.btnRemove.setOnClickListener(v -> {
            handleRemoveClick(mediaItem, holder);
        });
    }

    private void loadThumbnail(ImageView imageView, MediaItem mediaItem) {
        try {
            uriToLoad = mediaItem.isVideo() && mediaItem.getThumbnailUri() != null ?
                    mediaItem.getThumbnailUri() : mediaItem.getUri();

            Picasso.get()
                    .load(uriToLoad)
                    .error(R.mipmap.ic_launcher)
                    .fit()
                    .centerCrop()
                    .into(imageView, new com.squareup.picasso.Callback() {
                        @Override
                        public void onSuccess() {
                            // Success - nothing to do
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("GalleryAdapter", "Error loading thumbnail: " + uriToLoad, e);
                            // Fall back to main URI if thumbnail failed
                            if (mediaItem.isVideo() && !uriToLoad.equals(mediaItem.getUri())) {
                                Picasso.get()
                                        .load(mediaItem.getUri())
                                        .error(R.mipmap.ic_launcher)
                                        .fit()
                                        .centerCrop()
                                        .into(imageView);
                            }
                        }
                    });
        } catch (Exception e) {
            Log.e("GalleryAdapter", "Error setting up Picasso load", e);
        }
    }

    private void updateSelectionUI(ViewHolder holder, int selectionIndex) {
        if (selectionIndex != -1) {
            // Media is selected
            holder.selectionIndicator.setVisibility(View.VISIBLE);
            holder.selectionIndicator.setText(String.valueOf(selectionIndex + 1));
            holder.btnRemove.setVisibility(View.VISIBLE);
        } else {
            // Media is not selected
            holder.selectionIndicator.setVisibility(View.GONE);
            holder.btnRemove.setVisibility(View.GONE);
        }
    }

    private void handleMediaClick(MediaItem mediaItem, ViewHolder holder) {
        int currentIndex = selectedMediaItems.indexOf(mediaItem);

        if (currentIndex != -1) {
            // Media is already selected, update display to show this item
            callback.updateDisplayState(currentIndex);
            return;
        }

        // Check if we've reached the maximum limit
        if (selectedMediaItems.size() >= MAX_SELECTIONS) {
            callback.onMaxSelectionsReached();
            return;
        }

        // Media is not selected, add it
        selectedMediaItems.add(mediaItem);
        int newIndex = selectedMediaItems.size() - 1;
        updateSelectionUI(holder, newIndex);

        // Important: Update the display immediately when selecting new media
        callback.updateDisplayState(newIndex);
        callback.onMediaSelectionChanged(mediaItem, true);

        notifyDataSetChanged();
    }

    public int getRemainingSelections() {
        return MAX_SELECTIONS - selectedMediaItems.size();
    }

    public boolean hasReachedMaxSelections() {
        return selectedMediaItems.size() >= MAX_SELECTIONS;
    }

    private void handleRemoveClick(MediaItem mediaItem, ViewHolder holder) {
        int currentIndex = selectedMediaItems.indexOf(mediaItem);
        if (currentIndex != -1) {
            // Remove the media item
            selectedMediaItems.remove(mediaItem);
            updateSelectionUI(holder, -1);

            // Update the display state
            if (selectedMediaItems.isEmpty()) {
                callback.updateDisplayState(-1);
            } else {
                // If we removed a media item before the current display, adjust the index
                int currentDisplayed = Math.min(currentIndex, selectedMediaItems.size() - 1);
                callback.updateDisplayState(currentDisplayed);
            }

            callback.onMediaSelectionChanged(mediaItem, false);
            notifyDataSetChanged(); // Update all items to refresh selection numbers
        }
    }

    @Override
    public int getItemCount() {
        return mediaItems.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView selectionIndicator;
        ImageButton btnRemove;
        ImageView videoIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            selectionIndicator = itemView.findViewById(R.id.txtNumber);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            videoIndicator = itemView.findViewById(R.id.videoIndicator);
        }
    }
}