package com.alertify.monitorservice.application.query.dto.monitor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record MonitorResponse(
        String id,
        String tenantId,
        String sourceType,
        String url,
        Map<String, String> params,
        List<RuleResponse> rules,
        Map<String, Object> notifyPolicy,
        String status,
        Instant createdAt
) {
    public record RuleResponse(
            String id,
            String type,
            Map<String, Object> config
    ) {}
}