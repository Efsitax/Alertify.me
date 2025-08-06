package com.alertify.worker.domain.exception;

import com.alertify.common.domain.exception.DomainException;

public class FetcherException extends DomainException {

    public FetcherException(String message) {
        super("FETCHER_ERROR", message);
    }

    public FetcherException(String message, Throwable cause) {
        super("FETCHER_ERROR", message, cause);
    }
}
