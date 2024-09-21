package com.example.filoangler.Model;

public class PostModel {

    private String PostId;
    private String ImageURL;
    private String Description;
    private String Author;

    public PostModel(){

    }

    public PostModel(String postId, String imageURL, String description, String author) {
        PostId = postId;
        ImageURL = imageURL;
        Description = description;
        Author = author;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public String getImageURL() {
        return ImageURL;
    }

    public void setImageURL(String imageURL) {
        ImageURL = imageURL;
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

}
