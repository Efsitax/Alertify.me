package com.alertify.monitor.adapter.in.rest.query;

import com.alertify.monitor.application.query.dto.monitor.MonitorResponse;
import com.alertify.monitor.application.query.handler.monitor.GetMonitorByIdHandler;
import com.alertify.monitor.application.query.handler.monitor.ListMonitorsHandler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitors")
@RequiredArgsConstructor
public class MonitorQueryController {

    private final ListMonitorsHandler listMonitorsHandler;
    private final GetMonitorByIdHandler getMonitorByIdHandler;

    @GetMapping
    @Operation(
            summary = "List all monitors",
            description = "Retrieves a list of all monitors, optionally filtered by status. " +
                    "Use this endpoint to get an overview of all configured monitoring targets."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of monitors retrieved successfully",
                    content = @Content(
                            array = @ArraySchema(schema = @Schema(implementation = MonitorResponse.class))
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status parameter"
            )
    })
    public ResponseEntity<List<MonitorResponse>> listMonitors(
            @Parameter(
                    description = "Filter monitors by status. Valid values: ACTIVE, PASSIVE, DISABLED",
                    example = "ACTIVE"
            )
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(listMonitorsHandler.handle(status));
    }

    @GetMapping("/{monitorId}")
    @Operation(
            summary = "Get monitor by ID",
            description = "Retrieves detailed information about a specific monitor, including its configuration, " +
                    "rules, notification policy, and current status."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Monitor details retrieved successfully",
                    content = @Content(schema = @Schema(implementation = MonitorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Monitor not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<MonitorResponse> getMonitor(
            @Parameter(description = "Unique monitor identifier", required = true)
            @PathVariable String monitorId) {
        return ResponseEntity.ok(getMonitorByIdHandler.handle(monitorId));
    }

    public record ErrorResponse(
            @Schema(description = "Error code", example = "MONITOR_NOT_FOUND")
            String errorCode,
            @Schema(description = "Error message", example = "Monitor with id abc-123 not found")
            String message,
            @Schema(description = "Timestamp", example = "2025-08-03T10:30:00Z")
            String timestamp
    ) {}
}