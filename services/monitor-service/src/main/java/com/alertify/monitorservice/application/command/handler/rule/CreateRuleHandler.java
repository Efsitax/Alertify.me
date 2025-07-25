package com.alertify.monitorservice.application.command.handler.rule;

import com.alertify.monitorservice.application.command.dto.rule.CreateRuleRequest;
import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.entity.Rule;
import com.alertify.monitorservice.domain.exception.MonitorNotFoundException;
import com.alertify.monitorservice.domain.exception.RuleValidationException;
import com.alertify.monitorservice.domain.repository.MonitorRepository;
import com.alertify.monitorservice.domain.repository.RuleRepository;

import java.util.UUID;

public class CreateRuleHandler {

    private final MonitorRepository monitorRepository;
    private final RuleRepository ruleRepository;

    public CreateRuleHandler(MonitorRepository monitorRepository, RuleRepository ruleRepository) {
        this.monitorRepository = monitorRepository;
        this.ruleRepository = ruleRepository;
    }

    public Rule handle(CreateRuleRequest request) {
        Monitor monitor = monitorRepository.findById(request.monitorId())
                .orElseThrow(() -> new MonitorNotFoundException(UUID.fromString(request.monitorId())));

        if (request.type() == null || request.type().isBlank()) {
            throw new RuleValidationException("Rule type cannot be empty");
        }

        if (request.config() == null || request.config().isEmpty()) {
            throw new RuleValidationException("Rule config cannot be empty");
        }

        Rule rule = Rule.builder()
                .id(UUID.randomUUID())
                .monitor(monitor)
                .type(request.type())
                .config(request.config())
                .build();
        return ruleRepository.save(rule);
    }
}
