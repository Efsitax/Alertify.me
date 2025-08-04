package com.alertify.monitor.application.query.dto.monitor;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Schema(description = "Monitor information with current status")
public record MonitorResponse(
        @Schema(description = "Unique monitor identifier",
                example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Tenant ID owning this monitor",
                example = "550e8400-e29b-41d4-a716-446655440000")
        String tenantId,

        @Schema(description = "Type of data source being monitored",
                example = "ECOMMERCE_PRODUCT")
        String sourceType,

        @Schema(description = "URL of the monitored resource",
                example = "https://www.trendyol.com/product/12345")
        String url,

        @Schema(description = "Source-specific parameters")
        Map<String, String> params,

        @Schema(description = "Rules configured for this monitor")
        List<RuleResponse> rules,

        @Schema(description = "Notification policy configuration")
        Map<String, Object> notifyPolicy,

        @Schema(description = "Current monitor status",
                example = "ACTIVE",
                allowableValues = {"ACTIVE", "PASSIVE", "DISABLED", "ERROR"})
        String status,

        @Schema(description = "Monitor creation timestamp",
                example = "2025-08-03T10:30:00Z")
        Instant createdAt
) {
    @Schema(description = "Rule configuration for a monitor")
    public record RuleResponse(
            @Schema(description = "Unique rule identifier",
                    example = "550e8400-e29b-41d4-a716-446655440000")
            String id,

            @Schema(description = "Type of rule",
                    example = "TARGET_PRICE",
                    allowableValues = {"TARGET_PRICE", "PERCENT_DROP", "PERCENT_RISE", "WINDOW_COUNT"})
            String type,

            @Schema(description = "Rule-specific configuration",
                    example = """
                    {
                      "targetPrice": 1200,
                      "currency": "TRY"
                    }
                    """)
            Map<String, Object> config
    ) {}
}