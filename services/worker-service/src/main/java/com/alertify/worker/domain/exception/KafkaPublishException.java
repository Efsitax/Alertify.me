package com.alertify.worker.domain.exception;

import com.alertify.common.domain.exception.DomainException;

public class KafkaPublishException extends DomainException {
    public KafkaPublishException(String message, Throwable cause) {
        super("KAFKA_PUBLISH_ERROR", message, cause);
    }
}