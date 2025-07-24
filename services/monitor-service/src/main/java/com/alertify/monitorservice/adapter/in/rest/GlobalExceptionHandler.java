package com.alertify.monitorservice.adapter.in.rest;

import com.alertify.common.domain.exception.DomainException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<Map<String, Object>> handleDomain(DomainException ex) {
        log.warn("Domain error: {}", ex.getMessage());

        HttpStatus status = switch (ex.getErrorCode()) {
            case "MONITOR_NOT_FOUND", "RULE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "VALIDATION_FAILED" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.UNPROCESSABLE_ENTITY;
        };

        return ResponseEntity.status(status).body(Map.of(
                "timestamp", Instant.now().toString(),
                "errorCode", ex.getErrorCode(),
                "message", ex.getMessage()
        ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
                "timestamp", Instant.now().toString(),
                "errorCode", "INTERNAL_ERROR",
                "message", "Unexpected error occurred"
        ));
    }
}
