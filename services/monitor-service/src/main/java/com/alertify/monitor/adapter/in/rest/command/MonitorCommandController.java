package com.alertify.monitor.adapter.in.rest.command;

import com.alertify.monitor.application.command.dto.monitor.UpdateMonitorRequest;
import com.alertify.monitor.application.command.handler.monitor.CreateMonitorHandler;
import com.alertify.monitor.application.command.dto.monitor.CreateMonitorRequest;
import com.alertify.monitor.application.command.handler.monitor.DeleteMonitorHandler;
import com.alertify.monitor.application.command.handler.monitor.UpdateMonitorHandler;
import com.alertify.monitor.application.mapper.MonitorMapper;
import com.alertify.monitor.application.query.dto.monitor.MonitorResponse;
import com.alertify.monitor.domain.entity.Monitor;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitors")
@RequiredArgsConstructor
@Tag(name = "Monitor Management",
    description = "CRUD operations for monitors. Monitors define data sources to track (e-commerce products, crypto prices, etc.) and their associated rules.")
public class MonitorCommandController {

    private final CreateMonitorHandler createMonitorHandler;
    private final UpdateMonitorHandler updateMonitorHandler;
    private final DeleteMonitorHandler deleteMonitorHandler;
    private final MonitorMapper monitorMapper;

    @PostMapping
    @Operation(
            summary = "Create a new monitor",
            description = "Creates a new monitor to track a specific data source (e.g., product price, crypto rate). " +
                    "The monitor will be activated and start collecting data according to the configured rules."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Monitor created successfully",
                    content = @Content(schema = @Schema(implementation = MonitorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data or validation error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Monitor with this URL already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<MonitorResponse> createMonitor(
            @Parameter(description = "Monitor configuration data", required = true)
            @RequestBody CreateMonitorRequest request) {
        Monitor monitor = createMonitorHandler.handle(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(monitorMapper.toDto(monitor));
    }

    @PutMapping("/{monitorId}")
    @Operation(
            summary = "Update an existing monitor",
            description = "Updates the configuration of an existing monitor. This includes source parameters, " +
                    "notification policies, and associated rules."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Monitor updated successfully",
                    content = @Content(schema = @Schema(implementation = MonitorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Monitor not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<MonitorResponse> updateMonitor(
            @Parameter(description = "Monitor ID", required = true)
            @PathVariable String monitorId,
            @Parameter(description = "Updated monitor configuration", required = true)
            @RequestBody UpdateMonitorRequest request) {
        Monitor monitor = updateMonitorHandler.handle(monitorId, request);
        return ResponseEntity.ok(monitorMapper.toDto(monitor));
    }

    @DeleteMapping("/{monitorId}")
    @Operation(
            summary = "Delete a monitor",
            description = "Permanently deletes a monitor and stops all data collection. " +
                    "This action cannot be undone. Associated alerts and snapshots will be preserved."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "204",
                    description = "Monitor deleted successfully"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Monitor not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cannot delete monitor due to dependencies",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<Void> deleteMonitor(
            @Parameter(description = "Monitor ID to delete", required = true)
            @PathVariable String monitorId) {
        deleteMonitorHandler.handle(monitorId);
        return ResponseEntity.noContent().build();
    }

    @Schema(description = "Error response")
    public record ErrorResponse(
            @Schema(description = "Error code", example = "VALIDATION_ERROR")
            String errorCode,
            @Schema(description = "Error message", example = "Monitor URL cannot be empty")
            String message,
            @Schema(description = "Timestamp", example = "2025-08-03T10:30:00Z")
            String timestamp
    ) {}
}
