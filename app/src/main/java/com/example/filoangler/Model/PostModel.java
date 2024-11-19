package com.example.filoangler.Model;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;

public class PostModel {

    private String PostId;
    private String Description;
    private String Author;
    private String DatePosted;

    private ArrayList<String> mediaURLs;
    private ArrayList<Boolean> isVideoFlags;

    public PostModel(){

    }

    public PostModel(String postId, List<String> ImageURLs, String description, String author, String datePosted) {
        PostId = postId;
        ImageURLs = ImageURLs;
        Description = description;
        Author = author;
        DatePosted = datePosted;
    }

    public PostModel(DataSnapshot snapshot) {
        this.PostId = snapshot.child("PostId").getValue(String.class);

        this.mediaURLs = new ArrayList<>();
        this.isVideoFlags = new ArrayList<>();

        DataSnapshot mediaUrlsSnapshot = snapshot.child("MediaURLs");
        for (DataSnapshot urlSnapshot : mediaUrlsSnapshot.getChildren()) {
            String url = urlSnapshot.getValue(String.class);
            this.mediaURLs.add(url);
            // You might need to determine video/image type differently based on your upload logic
            this.isVideoFlags.add(url.contains(".mp4"));
        }

        this.Description = snapshot.child("Description").getValue(String.class);
        this.Author = snapshot.child("Author").getValue(String.class);
        this.DatePosted = snapshot.child("DatePosted").getValue(String.class);
    }

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

    public ArrayList<String> getMediaURLs() { return mediaURLs; }
    public ArrayList<Boolean> getIsVideoFlags() { return isVideoFlags; }
}
