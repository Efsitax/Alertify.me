package com.alertify.worker.domain.exception;

public class ClientException extends RuntimeException {
    public ClientException(String client, String message) {
        super(message);
    }

    public ClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
