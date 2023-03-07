package com.thoughtworks.exceptions;

import com.thoughtworks.errorcodes.InternalErrorCodes;

public class DependencyException extends ServiceException {

    final String dependencyType;
    final InternalErrorCodes dependencyName;
    final String dependencyURI;
    final String dependencyError;

    public DependencyException(String dependencyType, InternalErrorCodes dependencyName, String dependencyURI, String dependencyError, Exception causedByException) {
        super(dependencyName, dependencyError, causedByException);
        this.dependencyType = dependencyType;
        this.dependencyName = dependencyName;
        this.dependencyURI = dependencyURI;
        this.dependencyError = dependencyError;
    }

    @Override
    public InternalErrorCodes getErrorCode() {
        return dependencyName;
    }

    @Override
    public String getErrorMessage() {
        return dependencyError;
    }
}
