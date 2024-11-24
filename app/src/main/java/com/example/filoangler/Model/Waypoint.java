package com.example.filoangler.Model;

public class Waypoint {
    private String id;
    private String description;
    private int timestamp;

    public Waypoint(String id, String description, int timestamp) {
        this.id = id;
        this.description = description;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }

    public int getTimestamp() {
        return timestamp;
    }
}