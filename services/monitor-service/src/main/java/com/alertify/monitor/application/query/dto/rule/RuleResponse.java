package com.alertify.monitor.application.query.dto.rule;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Map;

@Schema(description = "Rule information and configuration")
public record RuleResponse(
        @Schema(description = "Unique rule identifier",
                example = "550e8400-e29b-41d4-a716-446655440000")
        String id,

        @Schema(description = "Monitor ID this rule belongs to",
                example = "550e8400-e29b-41d4-a716-446655440000")
        String monitorId,

        @Schema(description = "Rule type",
                example = "TARGET_PRICE",
                allowableValues = {"TARGET_PRICE", "PERCENT_DROP", "PERCENT_RISE", "WINDOW_COUNT"})
        String type,

        @Schema(description = "Rule configuration parameters",
                examples = {
                        """
                        {
                          "targetPrice": 1200,
                          "currency": "TRY"
                        }
                        """,
                        """
                        {
                          "percent": 15
                        }
                        """
                })
        Map<String, Object> config
) {}