package com.alertify.monitor.application.command.dto.monitor;

import com.alertify.monitor.domain.entity.Rule;

import java.util.List;
import java.util.Map;

public record UpdateMonitorRequest(
        String sourceType,
        String url,
        Map<String, String> params,
        List<Rule> rules,
        Map<String, Object> notifyPolicy,
        String status
) {
}
