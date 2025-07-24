package com.alertify.common.domain.exception;

public class NotFoundException extends DomainException{
    public NotFoundException(String resource, String identifier) {
        super(resource.toUpperCase() + "_NOT_FOUND", resource + " with id " + identifier + " not found");
    }
}
