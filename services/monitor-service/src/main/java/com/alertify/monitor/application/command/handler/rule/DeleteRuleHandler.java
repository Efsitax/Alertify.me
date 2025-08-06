package com.alertify.monitor.application.command.handler.rule;

import com.alertify.monitor.adapter.out.metrics.MonitorMetricsService;
import com.alertify.monitor.domain.exception.RuleNotFoundException;
import com.alertify.monitor.domain.repository.RuleRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Delete Rule Handler with metrics instrumentation
 */
@RequiredArgsConstructor
public class DeleteRuleHandler {

    private final RuleRepository ruleRepository;
    private final MonitorMetricsService metricsService;

    @Timed(value = "rule.delete", description = "Time taken to delete a rule")
    public void handle(String ruleId) {
        try {
            if (ruleRepository.findById(ruleId).isEmpty()) {
                metricsService.incrementNotFoundError();
                throw new RuleNotFoundException(UUID.fromString(ruleId));
            }

            ruleRepository.delete(ruleId);

            // Update metrics
            metricsService.incrementRuleDelete();
            updateTotalRulesCount();

        } catch (RuleNotFoundException e) {
            // Already counted
            throw e;
        }
    }

    private void updateTotalRulesCount() {
        try {
            // This would need a proper implementation to count all rules
            // For now, this is a placeholder
            metricsService.updateTotalRulesCount(0); // Placeholder
        } catch (Exception e) {
            // Don't fail the main operation due to metrics
        }
    }
}