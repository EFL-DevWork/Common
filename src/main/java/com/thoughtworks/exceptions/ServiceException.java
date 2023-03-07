package com.thoughtworks.exceptions;

import com.google.gson.JsonObject;
import com.thoughtworks.errorcodes.InternalErrorCodes;

public class ServiceException extends Exception {
    private final InternalErrorCodes errorCode;
    private final String errorMessage;
    private JsonObject details;


    public ServiceException(InternalErrorCodes errorCode, String errorMessage) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;

    }

    public ServiceException(InternalErrorCodes errorCode, String errorMessage, JsonObject details) {
        super(errorMessage);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.details = details;

    }

    public ServiceException(InternalErrorCodes errorCode, String errorMessage, Exception causedByException) {
        super(errorMessage, causedByException);
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;

    }

    public InternalErrorCodes getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public JsonObject getDetails() {
        return details;
    }

}
