package com.thoughtworks.exceptions;

import com.thoughtworks.errorcodes.InternalErrorCodes;

public class PaymentRefusedException extends BusinessException {
    public PaymentRefusedException(InternalErrorCodes errorCode, String errorMessage) {
        super(errorCode, errorMessage);
    }
}
