package com.thoughtworks.errorcodes;

public enum InternalErrorCodes {
    ACCOUNT_NOT_FOUND( "Account Details Not Found"),
    BANK_INFO_EXIST( "Bank info already exists"),
    SUSPECTED_ACCOUNT("Suspected fraudulent transaction"),
    INVALID_FIELD("Invalid input passed"),
    INVALID_IFSC_FORMAT("Invalid ifscCode format ->ABCD"),
    PAYMENT_REQUEST_NOT_READABLE("Request body missing or incorrect format"),
    BANK_INFO_NOT_FOUND("Bank info not found for "),
    SERVER_ERROR("Could not process the request"),
    SYSTEM_ERROR("System error"),
    USER_ALREADY_EXIST("User already added"),
    USER_NOT_FOUND("Invalid user Id or password");

    private final String description;

    InternalErrorCodes(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
