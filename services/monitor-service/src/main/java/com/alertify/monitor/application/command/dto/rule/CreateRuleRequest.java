package com.alertify.monitor.application.command.dto.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

@Schema(description = "Request to create a new rule for a monitor")
public record CreateRuleRequest(
        @Schema(description = "Monitor ID to attach this rule to",
                example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "Monitor ID is required")
        String monitorId,

        @Schema(description = "Type of rule to create",
                example = "TARGET_PRICE",
                allowableValues = {"TARGET_PRICE", "PERCENT_DROP", "PERCENT_RISE", "WINDOW_COUNT"})
        @NotBlank(message = "Rule type is required")
        String type,

        @Schema(description = "Rule configuration parameters",
                example = """
                {
                  "targetPrice": 1200,
                  "currency": "TRY"
                }
                """)
        @NotEmpty(message = "Rule configuration cannot be empty")
        Map<String, Object> config
) {}