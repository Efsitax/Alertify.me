package com.alertify.monitor.application.command.handler.monitor;

import com.alertify.monitor.adapter.out.metrics.MonitorMetricsService;
import com.alertify.monitor.domain.repository.MonitorRepository;
import com.alertify.monitor.domain.exception.MonitorNotFoundException;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class DeleteMonitorHandler {

    private final MonitorRepository repository;
    private final MonitorMetricsService metricsService;

    @Timed(value = "monitor.delete", description = "Time taken to delete a monitor")
    public void handle(String monitorId) {
        try {
            if (!repository.existsById(monitorId)) {
                metricsService.incrementNotFoundError();
                throw new MonitorNotFoundException(UUID.fromString(monitorId));
            }

            repository.delete(monitorId);

            // Update metrics
            metricsService.incrementMonitorDelete();
            updateActiveMonitorsCount();

        } catch (MonitorNotFoundException e) {
            // Already counted
            throw e;
        } catch (Exception e) {
            metricsService.incrementConflictError();
            throw new com.alertify.common.domain.exception.ConflictException("Monitor", "Cannot delete due to dependencies");
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
