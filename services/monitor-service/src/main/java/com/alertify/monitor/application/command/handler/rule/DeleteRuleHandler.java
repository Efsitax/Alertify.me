package com.alertify.monitor.application.command.handler.rule;

import com.alertify.monitor.domain.exception.RuleNotFoundException;
import com.alertify.monitor.domain.repository.RuleRepository;

import java.util.UUID;

public class DeleteRuleHandler {

    private final RuleRepository ruleRepository;

    public DeleteRuleHandler(RuleRepository ruleRepository) {
        this.ruleRepository = ruleRepository;
    }

    public void handle(String ruleId) {
        if (ruleRepository.findById(ruleId).isEmpty()) {
            throw new RuleNotFoundException(UUID.fromString(ruleId));
        }
        ruleRepository.delete(ruleId);
    }
}
