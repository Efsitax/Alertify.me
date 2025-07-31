package com.alertify.monitor.application.command.dto.rule;

import java.util.Map;

public record CreateRuleRequest (
    String monitorId,
    String type,
    Map<String, Object> config
) {}