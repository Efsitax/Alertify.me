package com.alertify.monitor.domain.exception;

import com.alertify.common.domain.exception.NotFoundException;

import java.util.UUID;

public class RuleNotFoundException extends NotFoundException {
    public RuleNotFoundException(UUID id) {
        super("Rule", id.toString());
    }
}
