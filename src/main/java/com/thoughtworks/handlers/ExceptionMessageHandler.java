package com.thoughtworks.handlers;

import com.thoughtworks.errorcodes.InternalErrorCodes;
import com.thoughtworks.exceptions.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageConversionException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static net.logstash.logback.argument.StructuredArguments.kv;

//@Hidden
@Slf4j
@ControllerAdvice
public class ExceptionMessageHandler {

    private static final String COULD_NOT_PROCESS_REQUEST = "Could not process the request";
    private static final String SERVER_ERROR = "SERVER_ERROR";
    private static final String INVALID_ERROR = "INVALID_INPUT";

    private void logException(Exception ex) {
        InternalErrorCodes eventCode;
        String descriptionString = "";
        String detailsString = "";
        String exceptionString = "";

        if (ServiceException.class.isAssignableFrom(ex.getClass())) {
            ServiceException serviceException = (ServiceException) ex;

            eventCode = serviceException.getErrorCode();
            descriptionString = serviceException.getErrorMessage();
            if (serviceException.getDetails() != null) {
                detailsString = serviceException.getDetails().toString();
            }
            exceptionString = serviceException.getClass().toString();
        } else {
            eventCode = InternalErrorCodes.SYSTEM_ERROR;
            descriptionString = ex.getMessage();
            exceptionString = ex.getClass().toString();
        }
        Throwable causedByException = ex.getCause();
        if ((causedByException) != null) {
            descriptionString = descriptionString + "=>" + causedByException.getMessage();
            exceptionString = exceptionString + "=>" + causedByException.getClass().toString();
        }
        
        log.error(descriptionString, kv("event_code", eventCode),
                kv("exception", exceptionString),
                kv("details", detailsString),
                kv("stackTraceElement", ex.getStackTrace().length > 0 ? ex.getStackTrace()[0].toString(): ""));
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorResponse handleResourceNotFoundException(ResourceNotFoundException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getErrorCode().toString(), ex.getErrorMessage());
        logException(ex);
        return new ErrorResponse().message("MISSING_INFO").reasons(errors);

    }

    @ExceptionHandler(ResourceConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorResponse handleConflictException(ResourceConflictException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getErrorCode().toString(), ex.getErrorMessage());
        logException(ex);
        return new ErrorResponse().message("REQUEST_CONFLICT").reasons(errors);
    }

    @ExceptionHandler(CallNotPermittedException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleCircuitBreakException(CallNotPermittedException callNotPermittedException) {
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.SERVER_ERROR.toString(), COULD_NOT_PROCESS_REQUEST);
        logException(callNotPermittedException);
        return new ErrorResponse().message(SERVER_ERROR).reasons(errors);
    }

    @ExceptionHandler({MethodArgumentNotValidException.class})
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleArgumentValidationException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();

        List<Map.Entry<String, String>> fieldErrorMessages = exception.getBindingResult().getFieldErrors().stream()
                .map(ExceptionMessageHandler::extractMessage)
                .collect(Collectors.toList());

        for (Map.Entry<String, String> errorMessage : fieldErrorMessages) {
            if (errors.containsKey(errorMessage.getKey())) {
                String updatedMsg = errorMessage.getKey() + "; " + errorMessage.getValue();
                errors.put(errorMessage.getKey(), updatedMsg);
            } else {
                errors.put(errorMessage.getKey(), errorMessage.getValue());
            }
        }

        logException(exception);
        return new ErrorResponse().message(INVALID_ERROR).reasons(errors);
    }

    private static Map.Entry<String, String> extractMessage(FieldError fieldError) {
        return new HashMap.SimpleEntry<>(fieldError.getField(), fieldError.getDefaultMessage());
    }

    @ExceptionHandler(ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleValidationException(ValidationException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getErrorCode().toString(), ex.getErrorMessage());
        logException(ex);
        return new ErrorResponse().message(INVALID_ERROR).reasons(errors);
    }

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    @ResponseBody
    public ErrorResponse handleProcessingException(BusinessException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put(ex.getErrorCode().toString(), ex.getErrorMessage());
        logException(ex);
        return new ErrorResponse().message("REQUEST_UNPROCESSABLE").reasons(errors);
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleGeneralException(Exception ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.SERVER_ERROR.toString(), COULD_NOT_PROCESS_REQUEST);
        logException(ex);
        return new ErrorResponse().message(SERVER_ERROR).reasons(errors);
    }

    @ExceptionHandler(DependencyException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorResponse handleDependencyException(DependencyException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.SERVER_ERROR.toString(), COULD_NOT_PROCESS_REQUEST);
        logException(ex);
        return new ErrorResponse().message(SERVER_ERROR).reasons(errors);
    }

    @ExceptionHandler(HttpMessageConversionException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorResponse handleRequestWithInvalidJson(HttpMessageConversionException ex) {
        Map<String, String> errors = new HashMap<>();
        errors.put(InternalErrorCodes.PAYMENT_REQUEST_NOT_READABLE.toString(), InternalErrorCodes.PAYMENT_REQUEST_NOT_READABLE.getDescription());
        logException(ex);
        return new ErrorResponse().message(INVALID_ERROR).reasons(errors);
    }
}
