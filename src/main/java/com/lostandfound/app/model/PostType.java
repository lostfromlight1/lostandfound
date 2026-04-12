package com.lostandfound.app.model;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PostType {
    LOST,
    FOUND;


    @JsonCreator
    public static PostType fromString(String value) {
        return PostType.valueOf(value.toUpperCase());
    }
}
