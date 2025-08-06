package com.alertify.monitor.application.command.handler.rule;

import com.alertify.monitor.adapter.out.metrics.MonitorMetricsService;
import com.alertify.monitor.application.command.dto.rule.UpdateRuleRequest;
import com.alertify.monitor.domain.entity.Rule;
import com.alertify.monitor.domain.exception.RuleNotFoundException;
import com.alertify.monitor.domain.exception.RuleValidationException;
import com.alertify.monitor.domain.repository.RuleRepository;
import io.micrometer.core.annotation.Timed;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Update Rule Handler with metrics instrumentation
 */
@RequiredArgsConstructor
public class UpdateRuleHandler {

    private final RuleRepository ruleRepository;
    private final MonitorMetricsService metricsService;

    @Timed(value = "rule.update", description = "Time taken to update a rule")
    public Rule handle(String ruleId, UpdateRuleRequest request) {
        try {
            Rule existing = ruleRepository.findById(ruleId)
                    .orElseThrow(() -> {
                        metricsService.incrementNotFoundError();
                        return new RuleNotFoundException(UUID.fromString(ruleId));
                    });

            if (request.type() == null || request.type().isBlank()) {
                metricsService.incrementValidationError();
                throw new RuleValidationException("Rule type cannot be empty");
            }

            if (request.config() == null || request.config().isEmpty()) {
                metricsService.incrementValidationError();
                throw new RuleValidationException("Rule config cannot be empty");
            }

            existing.setType(request.type());
            existing.setConfig(request.config());

            Rule updatedRule = ruleRepository.save(existing);

            // Update metrics
            metricsService.incrementRuleUpdate();

            return updatedRule;

        } catch (RuleNotFoundException e) {
            // Already counted
            throw e;
        } catch (RuleValidationException e) {
            // Already counted
            throw e;
        }
    }
}