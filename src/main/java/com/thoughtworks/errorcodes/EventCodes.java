package com.thoughtworks.errorcodes;

public enum EventCodes {
    PAYMENT_SUCCESSFUL("payment request successful"),
    REQUEST_RECEIVED("request received"),
    RESPONSE_SENT("response sent");

    private final String description;

    EventCodes(String description) {
        this.description = description;
    }
}
