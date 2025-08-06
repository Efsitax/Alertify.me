package com.alertify.worker.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "worker.fetcher")
public class FetcherConfigurationProperties {

    private String mode = "mock";
    private MockConfig mock = new MockConfig();
    private HttpConfig http = new HttpConfig();

    @Data
    public static class MockConfig {
        private double failureRate = 0.0;
        private int delayMin = 100;
        private int delayMax = 500;
        private double priceVolatility = 0.02;
        private double basePriceMin = 1000.0;
        private double basePriceMax = 3000.0;
    }

    @Data
    public static class HttpConfig {
        private long timeout = 10000;
        private int retryAttempts = 3;
        private int circuitBreakerThreshold = 5;
        private long circuitBreakWaitDuration = 30000;
    }
}
