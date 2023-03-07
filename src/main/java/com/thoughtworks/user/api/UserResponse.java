package com.thoughtworks.user.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserResponse {

    public static final String ADDED = "ADDED";
    public static final String UPDATED = "UPDATED";

    @JsonProperty("name")
    public final String name;

    @JsonProperty("message")
    public final String message;

    public UserResponse(String name, String status) {
        this.name = name;
        this.message = String.format("User details for %s %s successfully", name, status);
    }
}
