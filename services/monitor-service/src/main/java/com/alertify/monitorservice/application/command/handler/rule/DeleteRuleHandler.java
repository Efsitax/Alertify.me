package com.alertify.monitorservice.application.command.handler.rule;

import com.alertify.monitorservice.domain.exception.RuleNotFoundException;
import com.alertify.monitorservice.domain.repository.RuleRepository;

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
