package com.alertify.monitorservice.application.query.handler.monitor;

import com.alertify.monitorservice.application.mapper.MonitorMapper;
import com.alertify.monitorservice.application.query.dto.monitor.MonitorResponse;
import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.repository.MonitorRepository;

import java.util.List;

public class ListMonitorsHandler {

    private final MonitorRepository repository;
    private final MonitorMapper mapper;

    public ListMonitorsHandler(MonitorRepository repository, MonitorMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    public List<MonitorResponse> handle() {
        List<Monitor> monitors = repository.findAll();

        return monitors.stream()
                .map(mapper::toDto)
                .toList();
    }
}