package com.alertify.worker.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record MetricSample(
        String metric,
        BigDecimal value,
        String unit,
        Instant at
) {}