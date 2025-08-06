package com.alertify.worker.application.service;

import com.alertify.worker.adapter.out.http.FetcherServiceClient;
import com.alertify.worker.adapter.out.http.MonitorServiceClient;
import com.alertify.worker.domain.entity.Monitor;
import com.alertify.worker.domain.model.MetricSample;
import com.alertify.worker.domain.exception.ClientException;
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
        try {
            List<Monitor> activeMonitors = monitorServiceClient.fetchActiveMonitors();
            if (activeMonitors.isEmpty()) {
                log.info("No active monitors found.");
                return;
            }

            log.info("Fetched {} active monitors from monitor-service", activeMonitors.size());

            int successCount = 0;
            int failureCount = 0;

            for (Monitor monitor : activeMonitors) {
                try {
                    MetricSample sample = fetcherServiceClient.fetchMetric(monitor);
                    if (sample == null) {
                        log.warn("Failed to fetch metric for monitor {} - null response", monitor.getId());
                        failureCount++;
                        continue;
                    }

                    log.info("Fetched sample for monitor {}: {} {}",
                            monitor.getId(), sample.value(), sample.unit());

                    ruleEvaluator.evaluateAndProcess(monitor, sample);
                    successCount++;

                } catch (ClientException e) {
                    log.warn("Failed to fetch metric for monitor {} - client error: {}",
                            monitor.getId(), e.getMessage());
                    failureCount++;

                } catch (Exception e) {
                    log.error("Unexpected error processing monitor {}: {}",
                            monitor.getId(), e.getMessage(), e);
                    failureCount++;
                }
            }

            log.info("Batch processing completed: {} successful, {} failed", successCount, failureCount);

        } catch (Exception e) {
            log.error("Critical error during monitor batch processing: {}", e.getMessage(), e);
            throw new MetricProcessingException("Critical error during monitor batch processing: " + e.getMessage(), e);
        }
    }
}