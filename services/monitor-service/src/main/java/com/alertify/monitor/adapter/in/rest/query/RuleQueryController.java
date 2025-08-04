package com.alertify.monitor.adapter.in.rest.query;

import com.alertify.monitor.application.query.dto.rule.RuleResponse;
import com.alertify.monitor.application.query.handler.rule.GetRulesByMonitorIdHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/monitors/rules")
@RequiredArgsConstructor
@Tag(name = "Rule Queries",
        description = "Read operations for rules. Query and retrieve rule configurations for monitors.")
public class RuleQueryController {

    private final GetRulesByMonitorIdHandler getRulesByMonitorIdHandler;

    @GetMapping("/{monitorId}")
    @Operation(
            summary = "Get rules for a monitor",
            description = "Retrieves all rules configured for a specific monitor. " +
                    "Rules define the conditions that will trigger alerts for the monitor."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rules retrieved successfully",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = RuleResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Monitor not found or no rules found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    private ResponseEntity<List<RuleResponse>> getRule(@PathVariable("monitorId") String monitorId) {
        return ResponseEntity.ok(getRulesByMonitorIdHandler.handle(monitorId));
    }

    @Schema(description = "Error response")
    public record ErrorResponse(
            @Schema(description = "Error code", example = "RULE_NOT_FOUND")
            String errorCode,
            @Schema(description = "Error message", example = "No rules found for monitor abc-123")
            String message,
            @Schema(description = "Timestamp", example = "2025-08-03T10:30:00Z")
            String timestamp
    ) {}
}
