package com.example.filoangler.Adapter;

public interface GalleryAdapterCallback {
    void onImageSelectionChanged(String imagePath, boolean isSelected);
    void updateDisplayState(int currentImageDisplayed);
}
