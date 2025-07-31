package com.alertify.monitor.domain.exception;

import com.alertify.common.domain.exception.ValidationException;

public class RuleValidationException extends ValidationException {
    public RuleValidationException(String message) {
        super(message);
    }
}
