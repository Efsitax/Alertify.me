package com.alertify.monitorservice.application.command.dto.rule;

import java.util.Map;

public record UpdateRuleRequest (
        String type,
        Map<String, Object> config
){}
