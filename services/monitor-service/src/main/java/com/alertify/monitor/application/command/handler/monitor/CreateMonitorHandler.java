package com.alertify.monitor.application.command.handler.monitor;

import com.alertify.common.domain.exception.ConflictException;
import com.alertify.common.domain.exception.ValidationException;
import com.alertify.monitor.adapter.out.metrics.MonitorMetricsService;
import com.alertify.monitor.application.command.dto.monitor.CreateMonitorRequest;
import com.alertify.monitor.domain.entity.Monitor;
import com.alertify.monitor.domain.repository.MonitorRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class CreateMonitorHandler {

    private final MonitorRepository repository;
    private final MonitorMetricsService metricsService;

    @Timed(value = "monitor.create", description = "Time taken to create a monitor")
    public Monitor handle(CreateMonitorRequest request) {
        Timer.Sample sample = metricsService.startMonitorCreateTimer();

        try {
            if (request.url() == null) {
                metricsService.incrementValidationError();
                throw new ValidationException("Monitor must include a valid 'url' in params");
            }

            if (repository.existsByUrl(request.url())) {
                metricsService.incrementConflictError();
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

            Monitor savedMonitor = repository.save(monitor);

            // Update metrics
            metricsService.incrementMonitorCreate();
            updateActiveMonitorsCount();

            return savedMonitor;

        } finally {
            metricsService.stopMonitorCreateTimer(sample);
        }
    }

    private void updateActiveMonitorsCount() {
        try {
            long activeCount = repository.findByStatus("ACTIVE").size();
            metricsService.updateActiveMonitorsCount(activeCount);
        } catch (Exception e) {
            // Don't fail the main operation due to metrics
        }
    }
}
