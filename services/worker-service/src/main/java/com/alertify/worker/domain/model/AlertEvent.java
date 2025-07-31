package com.alertify.worker.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AlertEvent {
    private UUID alertId;
    private UUID monitorId;
    private UUID ruleId;
    private Instant firedAt;
    private String message;
}