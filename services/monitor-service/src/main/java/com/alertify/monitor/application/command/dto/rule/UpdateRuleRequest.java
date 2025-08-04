package com.alertify.monitor.application.command.dto.rule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

@Schema(description = "Request to update an existing rule")
public record UpdateRuleRequest(
        @Schema(description = "Updated rule type",
                example = "PERCENT_DROP",
                allowableValues = {"TARGET_PRICE", "PERCENT_DROP", "PERCENT_RISE", "WINDOW_COUNT"})
        @NotBlank(message = "Rule type is required")
        String type,

        @Schema(description = "Updated rule configuration",
                example = """
                {
                  "percent": 15,
                  "timeWindow": "1h"
                }
                """)
        @NotEmpty(message = "Rule configuration cannot be empty")
        Map<String, Object> config
) {}