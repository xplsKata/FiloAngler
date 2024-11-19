package com.example.filoangler.Adapter;

import com.example.filoangler.Model.MediaItem;

public interface GalleryAdapterCallback {
    void onMediaSelectionChanged(MediaItem mediaItem, boolean isSelected);
    void updateDisplayState(int currentMediaDisplayed);
    default void onMaxSelectionsReached() {} // Optional callback
}