package com.alertify.monitor.application.command.dto.rule;

import java.util.Map;

public record UpdateRuleRequest (
        String type,
        Map<String, Object> config
){}
