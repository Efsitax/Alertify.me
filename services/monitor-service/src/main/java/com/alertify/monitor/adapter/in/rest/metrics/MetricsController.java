package com.alertify.monitor.adapter.in.rest.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.search.Search;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.metrics.MetricsEndpoint;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/metrics")
@RequiredArgsConstructor
@Tag(name = "Custom Metrics",
        description = "Business metrics and performance indicators for Monitor Service")
public class MetricsController {

    private final MeterRegistry meterRegistry;
    private final MetricsEndpoint metricsEndpoint;

    @GetMapping("/business")
    @Operation(
            summary = "Get business metrics",
            description = "Returns Monitor Service specific business metrics including operation counts, " +
                    "error rates, and current system state."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Business metrics retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BusinessMetricsResponse.class))
            )
    })
    public ResponseEntity<BusinessMetricsResponse> getBusinessMetrics() {
        Map<String, Object> metrics = new HashMap<>();

        // Monitor operation metrics
        metrics.put("monitor_create_total", getCounterValue("monitor.operations", "operation", "create"));
        metrics.put("monitor_update_total", getCounterValue("monitor.operations", "operation", "update"));
        metrics.put("monitor_delete_total", getCounterValue("monitor.operations", "operation", "delete"));

        // Rule operation metrics
        metrics.put("rule_create_total", getCounterValue("rule.operations", "operation", "create"));
        metrics.put("rule_update_total", getCounterValue("rule.operations", "operation", "update"));
        metrics.put("rule_delete_total", getCounterValue("rule.operations", "operation", "delete"));

        // Error metrics
        metrics.put("validation_errors_total", getCounterValue("monitor.errors", "type", "validation"));
        metrics.put("not_found_errors_total", getCounterValue("monitor.errors", "type", "not_found"));
        metrics.put("conflict_errors_total", getCounterValue("monitor.errors", "type", "conflict"));

        // Current state gauges
        metrics.put("active_monitors_current", getGaugeValue("monitor.active.count"));
        metrics.put("total_rules_current", getGaugeValue("rule.total.count"));

        // Performance metrics (average response times in milliseconds)
        metrics.put("monitor_create_avg_duration_ms", getTimerMean("monitor.operation.duration", "operation", "create"));
        metrics.put("monitor_query_avg_duration_ms", getTimerMean("monitor.operation.duration", "operation", "query"));

        BusinessMetricsResponse response = new BusinessMetricsResponse(
                "Monitor Service Business Metrics",
                Instant.now(),
                metrics
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/summary")
    @Operation(
            summary = "Get metrics summary",
            description = "Returns a high-level summary of key performance indicators and system health metrics."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Metrics summary retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MetricsSummaryResponse.class))
            )
    })
    public ResponseEntity<MetricsSummaryResponse> getMetricsSummary() {
        // Calculate totals
        double totalMonitorOps = getCounterValue("monitor.operations", "operation", "create") +
                getCounterValue("monitor.operations", "operation", "update") +
                getCounterValue("monitor.operations", "operation", "delete");

        double totalRuleOps = getCounterValue("rule.operations", "operation", "create") +
                getCounterValue("rule.operations", "operation", "update") +
                getCounterValue("rule.operations", "operation", "delete");

        double totalErrors = getCounterValue("monitor.errors", "type", "validation") +
                getCounterValue("monitor.errors", "type", "not_found") +
                getCounterValue("monitor.errors", "type", "conflict");

        // Calculate error rate (percentage)
        double errorRate = totalMonitorOps > 0 ? (totalErrors / totalMonitorOps) * 100 : 0;

        MetricsSummaryResponse response = new MetricsSummaryResponse(
                (long) totalMonitorOps,
                (long) totalRuleOps,
                (long) totalErrors,
                String.format("%.2f%%", errorRate),
                (long) getGaugeValue("monitor.active.count"),
                (long) getGaugeValue("rule.total.count"),
                getTimerMean("monitor.operation.duration", "operation", "create"),
                Instant.now()
        );

        return ResponseEntity.ok(response);
    }

    // Helper methods to extract metric values
    private double getCounterValue(String name, String tagKey, String tagValue) {
        try {
            return Search.in(meterRegistry)
                    .name(name)
                    .tag(tagKey, tagValue)
                    .counter()
                    .count();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double getGaugeValue(String name) {
        try {
            return Search.in(meterRegistry)
                    .name(name)
                    .gauge()
                    .value();
        } catch (Exception e) {
            return 0.0;
        }
    }

    private double getTimerMean(String name, String tagKey, String tagValue) {
        try {
            return Search.in(meterRegistry)
                    .name(name)
                    .tag(tagKey, tagValue)
                    .timer()
                    .mean(java.util.concurrent.TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return 0.0;
        }
    }

    // Response DTOs
    @Schema(description = "Business metrics response")
    public record BusinessMetricsResponse(
            @Schema(description = "Service name", example = "Monitor Service Business Metrics")
            String service,

            @Schema(description = "Metrics collection timestamp")
            Instant timestamp,

            @Schema(description = "Business metrics data")
            Map<String, Object> metrics
    ) {}

    @Schema(description = "Metrics summary response")
    public record MetricsSummaryResponse(
            @Schema(description = "Total monitor operations", example = "150")
            Long totalMonitorOperations,

            @Schema(description = "Total rule operations", example = "89")
            Long totalRuleOperations,

            @Schema(description = "Total errors", example = "3")
            Long totalErrors,

            @Schema(description = "Error rate percentage", example = "2.34%")
            String errorRate,

            @Schema(description = "Current active monitors", example = "25")
            Long activeMonitors,

            @Schema(description = "Current total rules", example = "67")
            Long totalRules,

            @Schema(description = "Average response time in milliseconds", example = "45.67")
            Double avgResponseTimeMs,

            @Schema(description = "Summary generation timestamp")
            Instant timestamp
    ) {}
}