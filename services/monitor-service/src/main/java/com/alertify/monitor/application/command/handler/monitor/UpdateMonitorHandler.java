package com.alertify.monitor.application.command.handler.monitor;

import com.alertify.monitor.adapter.out.metrics.MonitorMetricsService;
import com.alertify.monitor.application.command.dto.monitor.UpdateMonitorRequest;
import com.alertify.monitor.domain.entity.Monitor;
import com.alertify.monitor.domain.repository.MonitorRepository;
import com.alertify.monitor.domain.exception.MonitorNotFoundException;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@RequiredArgsConstructor
public class UpdateMonitorHandler {

    private final MonitorRepository repository;
    private final MonitorMetricsService metricsService;

    @Timed(value = "monitor.update", description = "Time taken to update a monitor")
    public Monitor handle(String monitorId, UpdateMonitorRequest request) {
        try {
            Monitor existing = repository.findById(monitorId)
                    .orElseThrow(() -> {
                        metricsService.incrementNotFoundError();
                        return new MonitorNotFoundException(UUID.fromString(monitorId));
                    });

            existing.setSourceType(request.sourceType());
            existing.setParams(request.params());
            existing.setUrl(request.url());
            existing.setRules(request.rules());
            existing.setNotifyPolicy(request.notifyPolicy());
            existing.setStatus(request.status());
            existing.setCreatedAt(existing.getCreatedAt() != null ? existing.getCreatedAt() : Instant.now());

            Monitor updatedMonitor = repository.save(existing);

            metricsService.incrementMonitorUpdate();
            updateActiveMonitorsCount();

            return updatedMonitor;

        } catch (MonitorNotFoundException e) {

            throw e;
        } catch (Exception e) {
            metricsService.incrementValidationError();
            throw e;
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
