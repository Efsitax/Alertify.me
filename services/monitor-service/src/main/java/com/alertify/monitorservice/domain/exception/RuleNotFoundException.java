package com.alertify.monitorservice.domain.exception;

import com.alertify.common.domain.exception.NotFoundException;

import java.util.UUID;

public class RuleNotFoundException extends NotFoundException {
    public RuleNotFoundException(UUID id) {
        super("Rule", id.toString());
    }
}
