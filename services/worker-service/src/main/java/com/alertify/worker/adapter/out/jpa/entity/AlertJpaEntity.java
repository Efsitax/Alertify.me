package com.alertify.worker.adapter.out.jpa.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "alerts")
@Data
public class AlertJpaEntity {

    @Id
    private UUID id;

    @Column(name = "monitor_id", nullable = false)
    private UUID monitorId;

    @Column(name = "rule_id", nullable = false)
    private UUID ruleId;

    @Column(name = "fired_at", nullable = false)
    private Instant firedAt;

    @Column(columnDefinition = "TEXT")
    private String message;
}