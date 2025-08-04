package com.alertify.monitor.application.query.handler.monitor;

import com.alertify.monitor.application.mapper.MonitorMapper;
import com.alertify.monitor.application.query.dto.monitor.MonitorResponse;
import com.alertify.monitor.domain.entity.Monitor;
import com.alertify.monitor.domain.repository.MonitorRepository;

import java.util.List;

public class ListMonitorsHandler {

    private final MonitorRepository repository;
    private final MonitorMapper mapper;

    public ListMonitorsHandler(MonitorRepository repository, MonitorMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<MonitorResponse> handle(String status) {
        List<Monitor> monitors;

        if (status != null && !status.isBlank()) {
            monitors = repository.findByStatus(status);
        } else {
            monitors = repository.findAll();
        }

        return monitors.stream()
                .map(mapper::toDto)
                .toList();
    }
}