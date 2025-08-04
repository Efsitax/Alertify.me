package com.alertify.worker.domain.entity;

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
public class Alert {
    private UUID id;
    private UUID monitorId;
    private UUID ruleId;
    private Instant firedAt;
    private String message;
}
