package com.alertify.common.web.exception;

import com.alertify.common.domain.exception.ConflictException;
import com.alertify.common.domain.exception.DomainException;
import com.alertify.common.domain.exception.NotFoundException;
import com.alertify.common.domain.exception.ValidationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public record ErrorResponse(String errorCode, String message, Instant timestamp) { }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        log.warn("NotFoundException: {}", ex.getMessage());
        return buildResponse("NOT_FOUND", ex.getMessage(), org.springframework.http.HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException ex) {
        log.warn("ValidationException: {}", ex.getMessage());
        return buildResponse("VALIDATION_ERROR", ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
        log.warn("ConflictException: {}", ex.getMessage());
        return buildResponse("CONFLICT", ex.getMessage(), HttpStatus.CONFLICT);
    }

    @ExceptionHandler(DomainException.class)
    public  ResponseEntity<ErrorResponse> handleDomain(DomainException ex) {
        log.warn("DomainException: {}", ex.getMessage());
        return buildResponse(ex.getErrorCode(), ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception occurred", ex);
        return buildResponse("INTERNAL_ERROR", "Unexpected error occurred", HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> buildResponse(String code, String message, HttpStatus status) {
        return new ResponseEntity<>(
                new ErrorResponse(code, message, Instant.now()),
                status
        );
    }
}
