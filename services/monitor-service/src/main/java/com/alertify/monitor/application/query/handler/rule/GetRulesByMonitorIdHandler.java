package com.alertify.monitor.application.query.handler.rule;

import com.alertify.monitor.application.mapper.RuleMapper;
import com.alertify.monitor.application.query.dto.rule.RuleResponse;
import com.alertify.monitor.domain.exception.RuleNotFoundException;
import com.alertify.monitor.domain.repository.RuleRepository;

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
