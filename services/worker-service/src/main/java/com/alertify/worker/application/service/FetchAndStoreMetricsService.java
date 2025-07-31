package com.alertify.worker.application.service;

import com.alertify.worker.adapter.out.http.FetcherServiceClient;
import com.alertify.worker.adapter.out.http.MonitorServiceClient;
import com.alertify.worker.domain.entity.Monitor;
import com.alertify.worker.domain.model.MetricSample;
import com.alertify.worker.domain.exception.MetricProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FetchAndStoreMetricsService {

    private final MonitorServiceClient monitorServiceClient;
    private final FetcherServiceClient fetcherServiceClient;
    private final RuleEvaluator ruleEvaluator;

    public void runCheck() {
        List<Monitor> activeMonitors = monitorServiceClient.fetchActiveMonitors();
        if (activeMonitors.isEmpty()) {
            log.info("No active monitors found.");
            return;
        }

        log.info("Fetched {} active monitors from monitor-service", activeMonitors.size());
        try {
            for (Monitor monitor : activeMonitors) {
                MetricSample sample = fetcherServiceClient.fetchMetric(monitor);
                if (sample == null) {
                    log.warn("Failed to fetch metric for monitor {}", monitor.getId());
                    continue;
                }

                log.info("Fetched sample for monitor {}: {} {}",
                        monitor.getId(), sample.value(), sample.unit());

                ruleEvaluator.evaluateAndProcess(monitor, sample);
            }
        } catch (Exception e) {
            log.error("Error during metric fetching and processing: {}", e.getMessage(), e);
            throw new MetricProcessingException("Error during metric fetching and processing: " + e.getMessage(), e);
        }
    }
}