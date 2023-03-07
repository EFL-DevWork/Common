package com.thoughtworks.user.api;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.thoughtworks.user.model.Mask;
import com.thoughtworks.user.model.User;
import lombok.Getter;

@Getter
@JsonPropertyOrder({"userId", "firstName", "lastName", "dob", "taxId"})
public class UserDetailsResp {

    @JsonProperty("userId")
    public String userId;

    public String firstName;

    public String lastName;

    public String dob;

    @JsonProperty("taxId")
    @Mask
    public String taxId;

    public UserDetailsResp(User user) {
        this.userId = user.getUserId();
        this.firstName = user.getFirstName();
        this.lastName = user.getLastName();
        this.dob = user.getDob();
        this.taxId = user.getTaxId();
    }
}
