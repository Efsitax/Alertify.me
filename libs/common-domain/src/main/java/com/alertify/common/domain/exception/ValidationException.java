package com.alertify.common.domain.exception;

public class ValidationException extends DomainException {
    public ValidationException(String message) {
        super("VALIDATION_FAILED", message);
    }
}
