package com.alertify.monitor.adapter.out.health;

import com.alertify.monitor.domain.repository.MonitorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

@Slf4j
@Component("monitorService")
@RequiredArgsConstructor
public class MonitorServiceHealthIndicator implements HealthIndicator {

    private final MonitorRepository monitorRepository;

    @Override
    public Health health() {
        try {
            long monitorCount = monitorRepository.findAll().size();

            return Health.up()
                    .withDetail("monitorCount", monitorCount)
                    .withDetail("repositoryStatus", "healthy")
                    .withDetail("service", "Monitor Service")
                    .withDetail("version", "1.0.0")
                    .build();

        } catch (Exception e) {
            log.error("Health check failed for Monitor Service", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("cause", e.getClass().getSimpleName())
                    .build();
        }
    }
}