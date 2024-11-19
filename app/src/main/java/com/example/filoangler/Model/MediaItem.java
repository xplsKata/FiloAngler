// MediaItem.java
package com.example.filoangler.Model;

import android.net.Uri;

public class MediaItem {
    private Uri uri;
    private Uri thumbnailUri; // For videos
    private boolean isVideo;
    private long duration; // For videos
    private String mimeType;
    private long dateAdded; // Add this field

    public MediaItem(Uri uri, boolean isVideo) {
        this.uri = uri;
        this.isVideo = isVideo;
    }

    public MediaItem(Uri uri, Uri thumbnailUri, boolean isVideo, long duration, String mimeType, long dateAdded) {
        this.uri = uri;
        this.thumbnailUri = thumbnailUri;
        this.isVideo = isVideo;
        this.duration = duration;
        this.mimeType = mimeType;
        this.dateAdded = dateAdded;
    }

    // Getters and setters
    public Uri getUri() { return uri; }
    public Uri getThumbnailUri() { return thumbnailUri; }
    public boolean isVideo() { return isVideo; }
    public long getDuration() { return duration; }
    public String getMimeType() { return mimeType; }
    public long getDateAdded() { return dateAdded; }
}