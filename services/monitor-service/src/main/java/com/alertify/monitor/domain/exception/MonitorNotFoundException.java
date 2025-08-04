package com.alertify.monitor.domain.exception;

import com.alertify.common.domain.exception.NotFoundException;

import java.util.UUID;

public class MonitorNotFoundException extends NotFoundException {
    public MonitorNotFoundException(UUID id) {
        super("Monitor", id.toString());
    }
}
