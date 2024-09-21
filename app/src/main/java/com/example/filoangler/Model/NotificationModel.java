package com.example.filoangler.Model;

public class NotificationModel {

    private String UserId;
    private String Description;
    private String PostId;
    private Boolean isPost;

    public NotificationModel(){

    }

    public NotificationModel(String userId, String description, String postId, Boolean isPost) {
        UserId = userId;
        Description = description;
        PostId = postId;
        this.isPost = isPost;
    }

    public String getUserId() {
        return UserId;
    }

    public void setUserId(String userId) {
        UserId = userId;
    }

    public String getDescription() {
        return Description;
    }

    public void setDescription(String description) {
        Description = description;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        PostId = postId;
    }

    public Boolean getisPost() {
        return isPost;
    }

    public void setisPost(Boolean post) {
        isPost = post;
    }
}
