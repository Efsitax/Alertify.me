package com.alertify.monitorservice.application.command.dto.monitor;

import com.alertify.monitorservice.domain.entity.Rule;

import java.util.List;
import java.util.Map;

public record UpdateMonitorRequest(
        String sourceType,
        Map<String, String> params,
        List<Rule> rules,
        Map<String, Object> notifyPolicy,
        String status
) {
}
