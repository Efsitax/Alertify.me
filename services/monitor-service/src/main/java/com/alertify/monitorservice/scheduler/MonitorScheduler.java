package com.alertify.monitorservice.scheduler;

import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.repository.MonitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MonitorScheduler {

    private final MonitorRepository monitorRepository;
    private final MetricFetcherPort fetcher;
    private final RuleEvaluator ruleEvaluator;

    @Scheduled(fixedDelay = 60000)
    public void runScheduledCheck() {
        List<Monitor> activeMonitors = monitorRepository.findByStatus("ACTIVE");
        log.info("Scheduler triggered. Checking {} monitors", activeMonitors.size());

        for (Monitor monitor : activeMonitors) {
            var sample = fetcher.fetch(monitor);
            log.info("Fetched sample for monitor {}: {} {}",
                    monitor.getId(), sample.value(), sample.unit());
            ruleEvaluator.evaluateAndNotify(monitor, sample);
        }
    }
}
