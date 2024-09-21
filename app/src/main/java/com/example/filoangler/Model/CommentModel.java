package com.example.filoangler.Model;

public class CommentModel {

    private String Comment;
    private String Author;
    private String PostId;
    private String CommentId;

    public CommentModel(){

    }

    public CommentModel(String comment, String author, String postId, String commentId) {
        this.Comment = comment;
        Author = author;
        this.PostId = postId;
        this.CommentId = commentId;
    }

    public String getComment() {
        return Comment;
    }

    public void setComment(String comment) {
        this.Comment = comment;
    }

    public String getAuthor() {
        return Author;
    }

    public void setAuthor(String author) {
        Author = author;
    }

    public String getPostId() {
        return PostId;
    }

    public void setPostId(String postId) {
        this.PostId = postId;
    }

    public String getCommentId() {
        return CommentId;
    }

    public void setCommentId(String commentId) {
        CommentId = commentId;
    }

}
