package com.alertify.monitor.application.command.handler.monitor;

import com.alertify.monitor.domain.repository.MonitorRepository;
import com.alertify.monitor.domain.exception.MonitorNotFoundException;

import java.util.UUID;

public class DeleteMonitorHandler {

    private final MonitorRepository repository;

    public DeleteMonitorHandler(MonitorRepository repository) {
        this.repository = repository;
    }

    public void handle(String monitorId) {
        if (!repository.existsById(monitorId)) {
            throw new MonitorNotFoundException(UUID.fromString(monitorId));
        }

        try {
            repository.delete(monitorId);
        } catch (Exception e) {
            throw new com.alertify.common.domain.exception.ConflictException("Monitor", "Cannot delete due to dependencies");
        }
    }
}
