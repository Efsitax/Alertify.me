package com.alertify.monitorservice.application.query.dto.rule;

import java.util.Map;

public record RuleResponse (
        String id,
        String monitorId,
        String type,
        Map<String, Object> config
)
{ }
