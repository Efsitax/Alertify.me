package com.alertify.monitor.adapter.out.metrics;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduled component to update gauge metrics periodically
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MetricsScheduledUpdater {

    private final MonitorMetricsService metricsService;
    private final JdbcTemplate jdbcTemplate;

    /**
     * Update gauge metrics every 30 seconds
     */
    @Scheduled(fixedRate = 30000) // 30 seconds
    public void updateGaugeMetrics() {
        try {
            updateActiveMonitorsCount();
            updateTotalRulesCount();
            updateMonitorsByStatus();
            log.debug("Gauge metrics updated successfully");
        } catch (Exception e) {
            log.warn("Failed to update gauge metrics", e);
        }
    }

    /**
     * Update detailed metrics every 5 minutes
     */
    @Scheduled(fixedRate = 300000) // 5 minutes
    public void updateDetailedMetrics() {
        try {
            updateMonitorsBySourceType();
            updateRulesByType();
            log.debug("Detailed metrics updated successfully");
        } catch (Exception e) {
            log.warn("Failed to update detailed metrics", e);
        }
    }

    private void updateActiveMonitorsCount() {
        try {
            Long activeCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM monitors WHERE status = 'ACTIVE'",
                    Long.class
            );
            metricsService.updateActiveMonitorsCount(activeCount != null ? activeCount : 0);
        } catch (Exception e) {
            log.warn("Failed to update active monitors count", e);
        }
    }

    private void updateTotalRulesCount() {
        try {
            Long totalCount = jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM rules",
                    Long.class
            );
            metricsService.updateTotalRulesCount(totalCount != null ? totalCount : 0);
        } catch (Exception e) {
            log.warn("Failed to update total rules count", e);
        }
    }

    private void updateMonitorsByStatus() {
        try {
            // Count monitors by each status
            String[] statuses = {"ACTIVE", "PASSIVE", "DISABLED", "ERROR"};

            for (String status : statuses) {
                Long count = jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM monitors WHERE status = ?",
                        Long.class,
                        status
                );

                metricsService.recordCustomGauge(
                        "monitor.count.by.status",
                        "Number of monitors by status",
                        count != null ? count : 0,
                        "status", status.toLowerCase()
                );
            }
        } catch (Exception e) {
            log.warn("Failed to update monitors by status metrics", e);
        }
    }

    private void updateMonitorsBySourceType() {
        try {
            // Get distinct source types and their counts
            jdbcTemplate.query(
                    "SELECT source_type, COUNT(*) as count FROM monitors GROUP BY source_type",
                    (rs) -> {
                        while (rs.next()) {
                            String sourceType = rs.getString("source_type");
                            long count = rs.getLong("count");

                            metricsService.recordCustomGauge(
                                    "monitor.count.by.source.type",
                                    "Number of monitors by source type",
                                    count,
                                    "source_type", sourceType.toLowerCase()
                            );
                        }
                        return null;
                    }
            );
        } catch (Exception e) {
            log.warn("Failed to update monitors by source type metrics", e);
        }
    }

    private void updateRulesByType() {
        try {
            // Get distinct rule types and their counts
            jdbcTemplate.query(
                    "SELECT type, COUNT(*) as count FROM rules GROUP BY type",
                    (rs) -> {
                        while (rs.next()) {
                            String ruleType = rs.getString("type");
                            long count = rs.getLong("count");

                            metricsService.recordCustomGauge(
                                    "rule.count.by.type",
                                    "Number of rules by type",
                                    count,
                                    "rule_type", ruleType.toLowerCase()
                            );
                        }
                        return null;
                    }
            );
        } catch (Exception e) {
            log.warn("Failed to update rules by type metrics", e);
        }
    }
}