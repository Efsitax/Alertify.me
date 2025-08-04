package com.alertify.monitor.adapter.in.rest.health;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/health")
@RequiredArgsConstructor
@Tag(name = "Health Checks",
        description = "Health check endpoints for monitoring service status and dependencies")
public class HealthController {

    private final Map<String, HealthIndicator> healthIndicators;

    @GetMapping
    @Operation(
            summary = "Get overall service health",
            description = "Returns the overall health status of the Monitor Service including all dependencies. " +
                    "This endpoint is suitable for load balancer health checks."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Service is healthy",
                    content = @Content(schema = @Schema(implementation = HealthResponse.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "Service is unhealthy",
                    content = @Content(schema = @Schema(implementation = HealthResponse.class))
            )
    })
    public ResponseEntity<HealthResponse> health() {
        Map<String, Object> details = new HashMap<>();
        boolean overallHealthy = true;

        for (Map.Entry<String, HealthIndicator> entry : healthIndicators.entrySet()) {
            try {
                var health = entry.getValue().health();
                details.put(entry.getKey(), Map.of(
                        "status", health.getStatus().getCode(),
                        "details", health.getDetails()
                ));

                if (!"UP".equals(health.getStatus().getCode())) {
                    overallHealthy = false;
                }
            } catch (Exception e) {
                details.put(entry.getKey(), Map.of(
                        "status", "DOWN",
                        "error", e.getMessage()
                ));
                overallHealthy = false;
            }
        }

        HealthResponse response = new HealthResponse(
                overallHealthy ? "UP" : "DOWN",
                "Monitor Service",
                Instant.now(),
                details
        );

        HttpStatus httpStatus = overallHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(response);
    }

    @GetMapping("/ready")
    @Operation(
            summary = "Readiness probe",
            description = "Kubernetes readiness probe endpoint. Returns 200 when service is ready to accept traffic."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is ready"),
            @ApiResponse(responseCode = "503", description = "Service is not ready")
    })
    public ResponseEntity<ReadinessResponse> ready() {
        boolean isReady = true;

        if (healthIndicators.containsKey("database")) {
            try {
                var dbHealth = healthIndicators.get("database").health();
                isReady = "UP".equals(dbHealth.getStatus().getCode());
            } catch (Exception e) {
                isReady = false;
            }
        }

        ReadinessResponse response = new ReadinessResponse(
                isReady,
                isReady ? "UP" : "DOWN",
                "Monitor Service readiness check"
        );

        HttpStatus httpStatus = isReady ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(httpStatus).body(response);
    }

    @GetMapping("/live")
    @Operation(
            summary = "Liveness probe",
            description = "Kubernetes liveness probe endpoint. Returns 200 when service is alive and running."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Service is alive"),
            @ApiResponse(responseCode = "503", description = "Service is not responding")
    })
    public ResponseEntity<LivenessResponse> live() {
        LivenessResponse response = new LivenessResponse(
                true,
                "Monitor Service is alive",
                Instant.now()
        );

        return ResponseEntity.ok(response);
    }

    @Schema(description = "Health check response")
    public record HealthResponse(
            @Schema(description = "Health status", example = "UP")
            String status,

            @Schema(description = "Service name", example = "Monitor Service")
            String service,

            @Schema(description = "Check timestamp")
            Instant timestamp,

            @Schema(description = "Detailed health information")
            Map<String, Object> details
    ) {}

    @Schema(description = "Readiness check response")
    public record ReadinessResponse(
            @Schema(description = "Whether service is ready", example = "true")
            boolean ready,

            @Schema(description = "Health status", example = "UP")
            String status,

            @Schema(description = "Readiness message")
            String message
    ) {}

    @Schema(description = "Liveness check response")
    public record LivenessResponse(
            @Schema(description = "Whether service is alive", example = "true")
            boolean alive,

            @Schema(description = "Liveness message")
            String message,

            @Schema(description = "Check timestamp")
            Instant timestamp
    ) {}
}