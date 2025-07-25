package com.alertify.monitorservice.scheduler;

import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.scheduler.model.MetricSample;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;

@Component
public class FakeMetricFetcher implements MetricFetcherPort{

    private final Random random = new Random();

    @Override
    public MetricSample fetch(Monitor monitor) {
        BigDecimal value = BigDecimal.valueOf(1000 + random.nextInt(1000));
        return new MetricSample("price", value, "TRY", Instant.now());
    }
}
