package com.alertify.worker.application.job;

import com.alertify.worker.application.service.FetchAndStoreMetricsService;
import com.alertify.worker.domain.exception.SchedulerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkerScheduledJob {

    private final FetchAndStoreMetricsService fetchAndStoreMetricsService;

    @Scheduled(fixedDelayString = "${worker.schedule.interval:300000}")
    public void run() {
        try {
            fetchAndStoreMetricsService.runCheck();
        } catch (Exception e) {
            log.error("Error during scheduled job execution: {}", e.getMessage(), e);
            throw new SchedulerException("Error during scheduled job execution: " + e.getMessage(), e);
        }
    }
}