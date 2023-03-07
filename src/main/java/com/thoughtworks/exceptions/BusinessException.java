package com.thoughtworks.exceptions;

import com.thoughtworks.errorcodes.InternalErrorCodes;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
public class BusinessException extends ServiceException {
    public BusinessException(InternalErrorCodes errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
