package com.alertify.worker.domain.exception;

import com.alertify.common.domain.exception.DomainException;

public class MetricProcessingException extends DomainException {
    public MetricProcessingException(String message, Throwable cause) {
        super("METRIC_PROCESSING_ERROR", message, cause);
    }
}