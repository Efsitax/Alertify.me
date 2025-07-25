package com.alertify.monitorservice.adapter.out.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "snapshots")
@Data
public class SnapshotJpaEntity {

    @Id
    private UUID id;

    @Column(name = "monitor_id", nullable = false)
    private UUID monitorId;

    @Column(nullable = false)
    private String metric;

    @Column(nullable = false)
    private BigDecimal value;

    private String unit;

    @Column(nullable = false)
    private Instant at;
}
