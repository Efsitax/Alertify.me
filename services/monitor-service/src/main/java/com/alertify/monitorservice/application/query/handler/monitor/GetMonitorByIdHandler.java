package com.alertify.monitorservice.application.query.handler.monitor;

import com.alertify.monitorservice.application.mapper.MonitorMapper;
import com.alertify.monitorservice.application.query.dto.monitor.MonitorResponse;
import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.exception.MonitorNotFoundException;
import com.alertify.monitorservice.domain.repository.MonitorRepository;

import java.util.UUID;

public class GetMonitorByIdHandler {

    private final MonitorRepository repository;
    private final MonitorMapper mapper;

    public GetMonitorByIdHandler(MonitorRepository repository, MonitorMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public MonitorResponse handle(String id) {
        Monitor monitor = repository.findById(id)
                .orElseThrow(() -> new MonitorNotFoundException(UUID.fromString(id)));

        return mapper.toDto(monitor);
    }
}
