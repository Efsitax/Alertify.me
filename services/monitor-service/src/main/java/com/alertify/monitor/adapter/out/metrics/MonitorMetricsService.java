package com.alertify.monitor.adapter.out.metrics;

import io.micrometer.core.instrument.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class MonitorMetricsService {

    private final MeterRegistry meterRegistry;

    // Counters for API operations
    private final Counter monitorCreateCounter;
    private final Counter monitorUpdateCounter;
    private final Counter monitorDeleteCounter;
    private final Counter ruleCreateCounter;
    private final Counter ruleUpdateCounter;
    private final Counter ruleDeleteCounter;

    // Counters for errors
    private final Counter validationErrorCounter;
    private final Counter notFoundErrorCounter;
    private final Counter conflictErrorCounter;

    // Timers for operation performance
    private final Timer monitorCreateTimer;
    private final Timer ruleOperationTimer;

    // Gauges for current state
    private final AtomicLong activeMonitorsGauge = new AtomicLong(0);
    private final AtomicLong totalRulesGauge = new AtomicLong(0);

    public MonitorMetricsService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;

        // Initialize counters with proper static imports
        this.monitorCreateCounter = meterRegistry.counter("monitor.operations", "operation", "create");
        this.monitorUpdateCounter = meterRegistry.counter("monitor.operations", "operation", "update");
        this.monitorDeleteCounter = meterRegistry.counter("monitor.operations", "operation", "delete");
        this.ruleCreateCounter = meterRegistry.counter("rule.operations", "operation", "create");
        this.ruleUpdateCounter = meterRegistry.counter("rule.operations", "operation", "update");
        this.ruleDeleteCounter = meterRegistry.counter("rule.operations", "operation", "delete");

        // Initialize error counters
        this.validationErrorCounter = meterRegistry.counter("monitor.errors", "type", "validation");
        this.notFoundErrorCounter = meterRegistry.counter("monitor.errors", "type", "not_found");
        this.conflictErrorCounter = meterRegistry.counter("monitor.errors", "type", "conflict");

        // Initialize timers
        this.monitorCreateTimer = meterRegistry.timer("monitor.operation.duration", "operation", "create");
        this.ruleOperationTimer = meterRegistry.timer("rule.operation.duration");

        // Initialize gauges
        meterRegistry.gauge("monitor.active.count", this.activeMonitorsGauge, AtomicLong::get);
        meterRegistry.gauge("rule.total.count", this.totalRulesGauge, AtomicLong::get);
    }

    // Monitor operation metrics
    public void incrementMonitorCreate() {
        monitorCreateCounter.increment();
        log.debug("Monitor create counter incremented");
    }

    public void incrementMonitorUpdate() {
        monitorUpdateCounter.increment();
        log.debug("Monitor update counter incremented");
    }

    public void incrementMonitorDelete() {
        monitorDeleteCounter.increment();
        log.debug("Monitor delete counter incremented");
    }

    // Rule operation metrics
    public void incrementRuleCreate() {
        ruleCreateCounter.increment();
        log.debug("Rule create counter incremented");
    }

    public void incrementRuleUpdate() {
        ruleUpdateCounter.increment();
        log.debug("Rule update counter incremented");
    }

    public void incrementRuleDelete() {
        ruleDeleteCounter.increment();
        log.debug("Rule delete counter incremented");
    }

    // Error metrics
    public void incrementValidationError() {
        validationErrorCounter.increment();
        log.debug("Validation error counter incremented");
    }

    public void incrementNotFoundError() {
        notFoundErrorCounter.increment();
        log.debug("Not found error counter incremented");
    }

    public void incrementConflictError() {
        conflictErrorCounter.increment();
        log.debug("Conflict error counter incremented");
    }

    // Timer utilities
    public Timer.Sample startMonitorCreateTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopMonitorCreateTimer(Timer.Sample sample) {
        sample.stop(monitorCreateTimer);
    }

    public Timer.Sample startRuleOperationTimer() {
        return Timer.start(meterRegistry);
    }

    public void stopRuleOperationTimer(Timer.Sample sample) {
        sample.stop(ruleOperationTimer);
    }

    // Gauge updates
    public void updateActiveMonitorsCount(long count) {
        activeMonitorsGauge.set(count);
        log.debug("Active monitors gauge updated to: {}", count);
    }

    public void updateTotalRulesCount(long count) {
        totalRulesGauge.set(count);
        log.debug("Total rules gauge updated to: {}", count);
    }

    // Custom gauge method - simplified approach
    public void recordCustomGauge(String name, String description, double value, String... tags) {
        // Create a simple gauge with the current value
        meterRegistry.gauge(name, Tags.of(tags), value);
    }
}