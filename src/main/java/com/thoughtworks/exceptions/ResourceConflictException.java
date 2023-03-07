package com.thoughtworks.exceptions;

import com.thoughtworks.errorcodes.InternalErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class ResourceConflictException extends ServiceException {
    public ResourceConflictException(InternalErrorCodes errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
