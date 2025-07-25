package com.alertify.monitorservice.adapter.out.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

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

    private Instant firedAt;

    private String message;
}
