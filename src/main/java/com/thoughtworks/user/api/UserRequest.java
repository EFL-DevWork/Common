package com.thoughtworks.user.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserRequest {

    @JsonProperty("userId")
    public String userId;

    public String firstName;

    public String lastName;

    public String dob;

    @JsonProperty("taxId")
    public String taxId;

    @JsonProperty("password")
    public String password;

    public UserRequest(String userId, String firstName, String lastName, String dob, String taxId, String password) {
        this.userId = userId;
        this.firstName = firstName;
        this.lastName = lastName;
        this.dob = dob;
        this.taxId = taxId;
        this.password = password;
    }
}
