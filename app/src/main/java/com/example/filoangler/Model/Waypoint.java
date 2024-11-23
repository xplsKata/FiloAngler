package com.example.filoangler.Model;

public class Waypoint {
    private String title;
    private String description;
    private int timestamp; // in milliseconds

    public Waypoint(String title, String description, int timestamp) {
        this.title = title;
        this.description = description;
        this.timestamp = timestamp;
    }

    // Getters
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public int getTimestamp() { return timestamp; }
}
