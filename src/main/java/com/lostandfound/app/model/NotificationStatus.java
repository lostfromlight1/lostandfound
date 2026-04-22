package com.lostandfound.app.model;

public enum NotificationStatus {
    UNREAD("Not yet read"),
    READ("Already read");

    private final String description;

    NotificationStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
