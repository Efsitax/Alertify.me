package com.alertify.monitor.application.command.dto.monitor;

import com.alertify.monitor.domain.entity.Rule;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Map;

@Schema(description = "Request to create a new monitor")
public record CreateMonitorRequest(
        @Schema(description = "Tenant ID owning this monitor",
                example = "550e8400-e29b-41d4-a716-446655440000")
        @NotBlank(message = "Tenant ID is required")
        String tenantId,

        @Schema(description = "Type of data source to monitor",
                example = "ECOMMERCE_PRODUCT",
                allowableValues = {"ECOMMERCE_PRODUCT", "CRYPTO_TICKER", "FX_PAIR", "WEATHER_CITY"})
        @NotBlank(message = "Source type is required")
        String sourceType,

        @Schema(description = "URL of the resource to monitor",
                example = "https://www.trendyol.com/product/12345")
        @NotBlank(message = "URL is required")
        String url,

        @Schema(description = "Additional parameters for the data source",
                example = """
                {
                  "store": "Trendyol",
                  "currency": "TRY",
                  "selector": ".price-current"
                }
                """)
        Map<String, String> params,

        @Schema(description = "Initial rules to attach to this monitor")
        List<Rule> rules,

        @Schema(description = "Notification configuration",
                example = """
                {
                  "channels": ["EMAIL", "WEBHOOK"],
                  "throttleMinutes": 60,
                  "email": "alerts@example.com"
                }
                """)
        @NotNull(message = "Notification policy is required")
        Map<String, Object> notifyPolicy
) {}