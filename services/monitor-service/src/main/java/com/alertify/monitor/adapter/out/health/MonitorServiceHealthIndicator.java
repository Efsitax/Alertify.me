package com.alertify.monitor.adapter.out.health;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component("monitorService")
@RequiredArgsConstructor
public class MonitorServiceHealthIndicator implements HealthIndicator {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Health health() {
        try {
            Long monitorCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM monitors",
                    Long.class
            );

            Long ruleCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM rules",
                    Long.class
            );

            return Health.up()
                    .withDetail("monitorCount", monitorCount != null ? monitorCount : 0)
                    .withDetail("ruleCount", ruleCount != null ? ruleCount : 0)
                    .withDetail("repositoryStatus", "healthy")
                    .withDetail("service", "Monitor Service")
                    .withDetail("version", "1.0.0")
                    .build();

        } catch (Exception e) {
            log.error("Health check failed for Monitor Service", e);
            return Health.down()
                    .withDetail("error", e.getMessage())
                    .withDetail("cause", e.getClass().getSimpleName())
                    .withDetail("service", "Monitor Service")
                    .build();
        }
    }
}