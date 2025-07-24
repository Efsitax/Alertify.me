package com.alertify.monitorservice.application.query.handler.rule;

import com.alertify.monitorservice.application.mapper.RuleMapper;
import com.alertify.monitorservice.application.query.dto.rule.RuleResponse;
import com.alertify.monitorservice.domain.exception.RuleNotFoundException;
import com.alertify.monitorservice.domain.repository.RuleRepository;

import java.util.List;
import java.util.UUID;

public class GetRulesByMonitorIdHandler {

    private final RuleRepository ruleRepository;
    private final RuleMapper ruleMapper;

    public GetRulesByMonitorIdHandler(RuleRepository ruleRepository, RuleMapper ruleMapper) {
        this.ruleRepository = ruleRepository;
        this.ruleMapper = ruleMapper;
    }

    public List<RuleResponse> handle(String monitorId) {
        List<RuleResponse> rules = ruleRepository.findByMonitorId(monitorId)
                .stream()
                .map(ruleMapper::toResponse)
                .toList();

        if (rules.isEmpty()) {
            throw new RuleNotFoundException(UUID.fromString(monitorId));
        }

        return rules;
    }
}
