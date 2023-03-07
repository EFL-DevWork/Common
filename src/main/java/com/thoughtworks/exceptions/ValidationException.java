package com.thoughtworks.exceptions;

import com.thoughtworks.errorcodes.InternalErrorCodes;

public class ValidationException extends ServiceException {

    public ValidationException(InternalErrorCodes errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
