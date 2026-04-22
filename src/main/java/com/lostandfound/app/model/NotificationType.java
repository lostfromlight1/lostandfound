package com.lostandfound.app.model;

public enum NotificationType {
    COMMENT_CREATED("Comment on your post"),
    REPLY_CREATED("Reply to your comment"),
    REPLY_TO_REPLY("Reply to your reply"),
    POST_LIKED("Someone liked your post"),
    MENTION("You were mentioned"),
    POST_STATUS_CHANGED("Your post status changed");

    private final String description;

    NotificationType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
