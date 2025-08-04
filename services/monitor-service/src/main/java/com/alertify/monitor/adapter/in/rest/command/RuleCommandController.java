package com.alertify.monitor.adapter.in.rest.command;

import com.alertify.monitor.application.command.dto.rule.CreateRuleRequest;
import com.alertify.monitor.application.command.dto.rule.UpdateRuleRequest;
import com.alertify.monitor.application.command.handler.rule.CreateRuleHandler;
import com.alertify.monitor.application.command.handler.rule.DeleteRuleHandler;
import com.alertify.monitor.application.command.handler.rule.UpdateRuleHandler;
import com.alertify.monitor.application.mapper.RuleMapper;
import com.alertify.monitor.application.query.dto.rule.RuleResponse;
import com.alertify.monitor.domain.entity.Rule;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitors/rules")
@RequiredArgsConstructor
@Tag(name = "Rule Management",
        description = "CRUD operations for monitor rules. Rules define conditions that trigger alerts when met.")
public class RuleCommandController {

    private final CreateRuleHandler createRuleHandler;
    private final UpdateRuleHandler updateRuleHandler;
    private final DeleteRuleHandler deleteRuleHandler;
    private final RuleMapper ruleMapper;

    @PostMapping
    @Operation(
            summary = "Create a new rule",
            description = "Creates a new alert rule for a monitor. Rules define conditions that trigger alerts " +
                    "when specific thresholds or changes are detected in the monitored data."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Rule created successfully",
                    content = @Content(schema = @Schema(implementation = RuleResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid rule configuration",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Monitor not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<RuleResponse> createRule(
            @Parameter(
                    description = "Rule configuration",
                    required = true,
                    content = @Content(
                            examples = {
                                    @ExampleObject(
                                            name = "Target Price Rule",
                                            value = """
                                {
                                  "monitorId": "550e8400-e29b-41d4-a716-446655440000",
                                  "type": "TARGET_PRICE",
                                  "config": {
                                    "targetPrice": 1200,
                                    "currency": "TRY"
                                  }
                                }
                                """
                                    ),
                                    @ExampleObject(
                                            name = "Percent Drop Rule",
                                            value = """
                                {
                                  "monitorId": "550e8400-e29b-41d4-a716-446655440000",
                                  "type": "PERCENT_DROP",
                                  "config": {
                                    "percent": 15
                                  }
                                }
                                """
                                    )
                            }
                    )
            )
            @RequestBody CreateRuleRequest request
    ) {
        Rule created = createRuleHandler.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ruleMapper.toResponse(created));
    }

    @PutMapping("/{ruleId}")
    @Operation(
            summary = "Update an existing rule",
            description = "Updates the configuration of an existing rule. You can change the rule type, " +
                    "thresholds, and other parameters."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rule updated successfully",
                    content = @Content(schema = @Schema(implementation = RuleResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid rule configuration",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rule not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<RuleResponse> updateRule(
            @Parameter(description = "Rule ID", required = true)
            @PathVariable String ruleId,
            @Parameter(description = "Updated rule configuration", required = true)
            @RequestBody UpdateRuleRequest request) {
        Rule updated = updateRuleHandler.handle(ruleId, request);
        return ResponseEntity.ok(ruleMapper.toResponse(updated));
    }

    @DeleteMapping("/{ruleId}")
    @Operation(
            summary = "Delete a rule",
            description = "Permanently deletes a rule. Once deleted, the rule will no longer trigger alerts. " +
                    "This action cannot be undone."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Rule deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Rule not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteRule(
            @Parameter(description = "Rule ID to delete", required = true)
            @PathVariable String ruleId
    ) {
        deleteRuleHandler.handle(ruleId);
        return ResponseEntity.noContent().build();
    }

    @Schema(description = "Error response")
    public record ErrorResponse(
            @Schema(description = "Error code", example = "RULE_NOT_FOUND")
            String errorCode,
            @Schema(description = "Error message", example = "Rule with id abc-123 not found")
            String message,
            @Schema(description = "Timestamp", example = "2025-08-03T10:30:00Z")
            String timestamp
    ) {}
}