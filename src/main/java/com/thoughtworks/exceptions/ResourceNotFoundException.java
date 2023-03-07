package com.thoughtworks.exceptions;

import com.google.gson.JsonObject;
import com.thoughtworks.errorcodes.InternalErrorCodes;

public class ResourceNotFoundException extends ServiceException {
    public ResourceNotFoundException(InternalErrorCodes errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }

    public ResourceNotFoundException(InternalErrorCodes errorCode, String errorMessage, JsonObject details) {
        super(errorCode, errorMessage, details);
    }
}
