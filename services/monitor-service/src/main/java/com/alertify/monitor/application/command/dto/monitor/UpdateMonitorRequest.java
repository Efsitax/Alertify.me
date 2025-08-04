package com.alertify.monitor.application.command.dto.monitor;

import com.alertify.monitor.domain.entity.Rule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

import java.util.List;
import java.util.Map;

@Schema(description = "Request to update an existing monitor")
public record UpdateMonitorRequest(
        @Schema(description = "Type of data source to monitor",
                example = "ECOMMERCE_PRODUCT",
                allowableValues = {"ECOMMERCE_PRODUCT", "CRYPTO_TICKER", "FX_PAIR", "WEATHER_CITY"})
        @NotBlank(message = "Source type is required")
        String sourceType,

        @Schema(description = "URL of the resource to monitor",
                example = "https://www.trendyol.com/product/updated-12345")
        @NotBlank(message = "URL is required")
        String url,

        @Schema(description = "Updated parameters for the data source",
                example = """
                {
                  "store": "Trendyol",
                  "currency": "USD",
                  "selector": ".price-current"
                }
                """)
        Map<String, String> params,

        @Schema(description = "Updated rules for this monitor")
        List<Rule> rules,

        @Schema(description = "Updated notification configuration",
                example = """
                {
                  "channels": ["EMAIL", "SLACK"],
                  "throttleMinutes": 30,
                  "email": "alerts@example.com"
                }
                """)
        Map<String, Object> notifyPolicy,

        @Schema(description = "Monitor status",
                example = "ACTIVE",
                allowableValues = {"ACTIVE", "PASSIVE", "DISABLED"})
        String status
) {}