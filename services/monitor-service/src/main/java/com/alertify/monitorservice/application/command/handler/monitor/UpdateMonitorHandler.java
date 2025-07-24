package com.alertify.monitorservice.application.command.handler.monitor;

import com.alertify.monitorservice.application.command.dto.monitor.UpdateMonitorRequest;
import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.repository.MonitorRepository;
import com.alertify.monitorservice.domain.exception.MonitorNotFoundException;

import java.time.Instant;
import java.util.UUID;

public class UpdateMonitorHandler {

    private final MonitorRepository repository;

    public UpdateMonitorHandler(MonitorRepository repository) {
        this.repository = repository;
    }

    public Monitor handle(String monitorId, UpdateMonitorRequest request) {
        Monitor existing = repository.findById(monitorId)
                .orElseThrow(() -> new MonitorNotFoundException(UUID.fromString(monitorId)));

        existing.setSourceType(request.sourceType());
        existing.setParams(request.params());
        existing.setRules(request.rules());
        existing.setNotifyPolicy(request.notifyPolicy());
        existing.setStatus(request.status());
        existing.setCreatedAt(existing.getCreatedAt() != null ? existing.getCreatedAt() : Instant.now());

        return repository.save(existing);
    }
}
