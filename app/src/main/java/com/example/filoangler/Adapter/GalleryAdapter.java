package com.example.filoangler.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.filoangler.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.ViewHolder> {
    private Context context;
    private ArrayList<String> imagePaths;
    private ArrayList<String> selectedImagePaths;
    private GalleryAdapterCallback callback;

    public GalleryAdapter(Context context, ArrayList<String> imagePaths, ArrayList<String> selectedImagePaths) {
        this.context = context;
        this.imagePaths = imagePaths;
        this.selectedImagePaths = selectedImagePaths;
        if (context instanceof GalleryAdapterCallback) {
            this.callback = (GalleryAdapterCallback) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement GalleryAdapterCallback");
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String imagePath = imagePaths.get(position);

        Picasso.get()
                .load(imagePath)
                .error(R.mipmap.ic_launcher) // Add an error drawable
                .fit()
                .centerCrop()
                .into(holder.imageView);

        int selectionIndex = selectedImagePaths.indexOf(imagePath);
        updateSelectionUI(holder, selectionIndex);

        // Handle image click events
        holder.imageView.setOnClickListener(v -> {
            handleImageClick(imagePath, holder);
        });

        // Handle remove button click events
        holder.btnRemove.setOnClickListener(v -> {
            handleRemoveClick(imagePath, holder);
        });
    }

    private void updateSelectionUI(ViewHolder holder, int selectionIndex) {
        if (selectionIndex != -1) {
            // Image is selected
            holder.selectionIndicator.setVisibility(View.VISIBLE);
            holder.selectionIndicator.setText(String.valueOf(selectionIndex + 1));
            holder.btnRemove.setVisibility(View.VISIBLE);
        } else {
            // Image is not selected
            holder.selectionIndicator.setVisibility(View.GONE);
            holder.btnRemove.setVisibility(View.GONE);
        }
    }

    private void handleImageClick(String imagePath, ViewHolder holder) {
        int currentIndex = selectedImagePaths.indexOf(imagePath);

        if (currentIndex != -1) {
            // Image is already selected, do nothing as removal should be done via remove button
            return;
        } else {
            // Image is not selected, add it
            selectedImagePaths.add(imagePath);
            updateSelectionUI(holder, selectedImagePaths.size() - 1);
            callback.onImageSelectionChanged(imagePath, true);
            callback.updateDisplayState(selectedImagePaths.size() - 1);
        }
        notifyDataSetChanged(); // Update all items to refresh selection numbers
    }

    private void handleRemoveClick(String imagePath, ViewHolder holder) {
        int currentIndex = selectedImagePaths.indexOf(imagePath);
        if (currentIndex != -1) {
            // Remove the image
            selectedImagePaths.remove(imagePath);
            updateSelectionUI(holder, -1);

            // Update the display state
            if (selectedImagePaths.isEmpty()) {
                callback.updateDisplayState(-1);
            } else {
                // If we removed an image before the current display, adjust the index
                int currentDisplayed = Math.min(currentIndex, selectedImagePaths.size() - 1);
                callback.updateDisplayState(currentDisplayed);
            }

            callback.onImageSelectionChanged(imagePath, false);
            notifyDataSetChanged(); // Update all items to refresh selection numbers
        }
    }

    @Override
    public int getItemCount() {
        return imagePaths.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView selectionIndicator;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image);
            selectionIndicator = itemView.findViewById(R.id.txtNumber);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }
}

