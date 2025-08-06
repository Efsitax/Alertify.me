package com.alertify.monitor.application.command.handler.rule;

import com.alertify.monitor.adapter.out.metrics.MonitorMetricsService;
import com.alertify.monitor.application.command.dto.rule.CreateRuleRequest;
import com.alertify.monitor.domain.entity.Monitor;
import com.alertify.monitor.domain.entity.Rule;
import com.alertify.monitor.domain.exception.MonitorNotFoundException;
import com.alertify.monitor.domain.exception.RuleValidationException;
import com.alertify.monitor.domain.repository.MonitorRepository;
import com.alertify.monitor.domain.repository.RuleRepository;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Create Rule Handler with metrics instrumentation
 */
@RequiredArgsConstructor
public class CreateRuleHandler {

    private final MonitorRepository monitorRepository;
    private final RuleRepository ruleRepository;
    private final MonitorMetricsService metricsService;

    @Timed(value = "rule.create", description = "Time taken to create a rule")
    public Rule handle(CreateRuleRequest request) {
        Timer.Sample sample = metricsService.startRuleOperationTimer();

        try {
            Monitor monitor = monitorRepository.findById(request.monitorId())
                    .orElseThrow(() -> {
                        metricsService.incrementNotFoundError();
                        return new MonitorNotFoundException(UUID.fromString(request.monitorId()));
                    });

            if (request.type() == null || request.type().isBlank()) {
                metricsService.incrementValidationError();
                throw new RuleValidationException("Rule type cannot be empty");
            }

            if (request.config() == null || request.config().isEmpty()) {
                metricsService.incrementValidationError();
                throw new RuleValidationException("Rule config cannot be empty");
            }

            Rule rule = Rule.builder()
                    .id(UUID.randomUUID())
                    .monitor(monitor)
                    .type(request.type())
                    .config(request.config())
                    .build();

            Rule savedRule = ruleRepository.save(rule);

            // Update metrics
            metricsService.incrementRuleCreate();
            updateTotalRulesCount();

            return savedRule;

        } finally {
            metricsService.stopRuleOperationTimer(sample);
        }
    }

    private void updateTotalRulesCount() {
        try {
            // This is a simplified count - in production you might want to optimize this
            long totalRules = ruleRepository.findByMonitorId("").size(); // Placeholder
            metricsService.updateTotalRulesCount(totalRules);
        } catch (Exception e) {
            // Don't fail the main operation due to metrics
        }
    }
}