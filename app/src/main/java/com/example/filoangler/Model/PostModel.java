package com.example.filoangler.Model;

import java.util.List;

public class PostModel {

    private String PostId;
    private List<String> ImageURLs;
    private String Description;
    private String Author;
    private String DatePosted;

    public PostModel(){

    }

    public PostModel(String postId, List<String> ImageURLs, String description, String author, String datePosted) {
        PostId = postId;
        ImageURLs = ImageURLs;
        Description = description;
        Author = author;
        DatePosted = datePosted;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public List<String> getImageURLs() {
        return ImageURLs;
    }

    public void setImageURL(List<String> ImageURLs) {
        ImageURLs = ImageURLs;
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
}
