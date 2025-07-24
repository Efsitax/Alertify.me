package com.alertify.monitorservice.adapter.out.jpa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "monitors")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonitorJpaEntity {

    @Id
    @GeneratedValue
    private UUID id;

    @Column(name = "tenant_id", nullable = false)
    private UUID tenantId;

    @Column(name = "source_type", nullable = false)
    private String sourceType;

    @Column(nullable = false)
    private String url;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "params", columnDefinition = "jsonb")
    private Map<String, String> params;

    @OneToMany(mappedBy = "monitor", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RuleJpaEntity> rules;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "notify_policy", columnDefinition = "jsonb")
    private Map<String, Object> notifyPolicy;

    private String status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;
}