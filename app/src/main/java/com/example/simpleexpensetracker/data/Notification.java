package com.example.simpleexpensetracker.data;

public class Notification {
    private final String message;
    private final String timestamp;

    public Notification(String message, String timestamp) {
        this.message = message;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}
