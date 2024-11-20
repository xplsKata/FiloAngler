package com.example.filoangler.Model;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostModel {
    private String PostId;
    private String Description;
    private String Author;
    private String DatePosted;

    private ArrayList<MediaItem> mediaItems;

    public PostModel() {
        // Default constructor for Firebase
        mediaItems = new ArrayList<>();
    }

    public PostModel(DataSnapshot snapshot) {
        this.PostId = snapshot.getKey();
        this.mediaItems = new ArrayList<>();

        try {
            // First, try the new media structure
            if (snapshot.child("mediaURLs").exists()) {
                for (DataSnapshot mediaSnapshot : snapshot.child("mediaURLs").getChildren()) {
                    String url = mediaSnapshot.child("url").getValue(String.class);
                    Boolean isVideo = mediaSnapshot.child("isVideo").getValue(Boolean.class);

                    if (url != null && isVideo != null) {
                        this.mediaItems.add(new MediaItem(url, isVideo));
                    }
                }
            }
            // If no mediaUrls, try a fallback
            else if (snapshot.child("MediaURLs").exists()) {
                // Add fallback logic if your previous structure was different
                List<String> urls = (List<String>) snapshot.child("MediaURLs").getValue();
                if (urls != null) {
                    for (String url : urls) {
                        this.mediaItems.add(new MediaItem(url, false));  // Assume images by default
                    }
                }
            }

            this.Description = snapshot.child("Description").getValue(String.class);
            this.Author = snapshot.child("Author").getValue(String.class);
            this.DatePosted = snapshot.child("DatePosted").getValue(String.class);

            // Log details about the post
            Log.d("PostDebug", "Post constructed: " +
                    "ID=" + PostId +
                    ", Media Items=" + mediaItems.size() +
                    ", Author=" + Author);
        } catch (Exception e) {
            Log.e("PostModelError", "Error constructing PostModel: " + e.getMessage(), e);
        }
    }

    // Getters and setters
    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getDatePosted() {
        return DatePosted;
    }

    public void setDatePosted(String datePosted) {
        DatePosted = datePosted;
    }

    // Updated methods to work with MediaItem
    public ArrayList<MediaItem> getMediaItems() {
        return mediaItems;
    }

    public ArrayList<String> getMediaURLs() {
        ArrayList<String> urls = new ArrayList<>();
        for (MediaItem item : mediaItems) {
            urls.add(item.getUrl());
        }
        return urls;
    }

    public ArrayList<Boolean> getIsVideoFlags() {
        ArrayList<Boolean> flags = new ArrayList<>();
        for (MediaItem item : mediaItems) {
            flags.add(item.isVideo());
        }
        return flags;
    }
}
