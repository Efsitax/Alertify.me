package com.alertify.monitorservice.application.command.handler.rule;

import com.alertify.monitorservice.application.command.dto.rule.UpdateRuleRequest;
import com.alertify.monitorservice.domain.entity.Rule;
import com.alertify.monitorservice.domain.exception.RuleNotFoundException;
import com.alertify.monitorservice.domain.exception.RuleValidationException;
import com.alertify.monitorservice.domain.repository.RuleRepository;

import java.util.UUID;

public class UpdateRuleHandler {

    private final RuleRepository ruleRepository;

    public UpdateRuleHandler(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public Rule handle(String ruleId, UpdateRuleRequest request) {
        Rule existing = ruleRepository.findById(ruleId)
                .orElseThrow(() -> new RuleNotFoundException(UUID.fromString(ruleId)));

        if (request.type() == null || request.type().isBlank()) {
            throw new RuleValidationException("Rule type cannot be empty");
        }
        if (request.config() == null || request.config().isEmpty()) {
            throw new RuleValidationException("Rule config cannot be empty");
        }

        existing.setType(request.type());
        existing.setConfig(request.config());

        return ruleRepository.save(existing);
    }
}
