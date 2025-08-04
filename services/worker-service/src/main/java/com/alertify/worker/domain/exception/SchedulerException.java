package com.alertify.worker.domain.exception;

import com.alertify.common.domain.exception.DomainException;

public class SchedulerException extends DomainException {
    public SchedulerException(String message, Throwable cause) {
        super("SCHEDULER_ERROR", message, cause);
    }
}