package com.alertify.common.domain.exception;

public class ConflictException extends DomainException {
    public ConflictException(String resource, String reason) {
        super(resource.toUpperCase() + "_CONFLICT", resource + " conflict: " + reason);
    }
}
