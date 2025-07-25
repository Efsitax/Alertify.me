package com.alertify.monitorservice.domain.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Snapshot {
    private UUID id;
    private UUID monitorId;
    private String metric;
    private BigDecimal value;
    private String unit;
    private Instant at;
}
