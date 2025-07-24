package com.alertify.monitorservice.application.command.handler.monitor;

import com.alertify.common.domain.exception.ConflictException;
import com.alertify.common.domain.exception.ValidationException;
import com.alertify.monitorservice.application.command.dto.monitor.CreateMonitorRequest;
import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.repository.MonitorRepository;

import java.time.Instant;
import java.util.UUID;

public class CreateMonitorHandler {

    private final MonitorRepository repository;

    public CreateMonitorHandler(MonitorRepository repository) {
        this.repository = repository;
    }

    public Monitor handle(CreateMonitorRequest request) {
        if (request.url() == null) {
            throw new ValidationException("Monitor must include a valid 'url' in params");
        }

        if (repository.existsByUrl(request.url())) {
            throw new ConflictException("Monitor", "Monitor for this URL already exists");
        }

        Monitor monitor = Monitor.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.fromString(request.tenantId()))
                .sourceType(request.sourceType())
                .url(request.url())
                .params(request.params())
                .rules(request.rules())
                .notifyPolicy(request.notifyPolicy())
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        return repository.save(monitor);
    }
}
