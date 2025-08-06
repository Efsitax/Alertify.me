package com.alertify.worker.adapter.out.mock;

import com.alertify.worker.domain.entity.Monitor;
import com.alertify.worker.domain.exception.FetcherException;
import com.alertify.worker.domain.model.MetricSample;
import com.alertify.worker.domain.port.out.FetcherServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@Profile("!prod")
public class MockFetcherServiceClient implements FetcherServiceClient {

    private final Random random = new Random();
    private final Map<String, PriceSimulation> priceSimulations = new ConcurrentHashMap<>();

    @Override
    public MetricSample fetchMetric(Monitor monitor) {
        log.debug("Fetching mock metric for monitor: {}", monitor.getId());

        if (random.nextDouble() < 0.05) {
            throw new FetcherException("Simulated network error for monitor: " + monitor.getId());
        }

        try {
            Thread.sleep(100 + random.nextInt(400));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FetcherException("Interrupted while fetching metric", e);
        }

        String url = monitor.getParams().get("url");
        PriceSimulation simulation = priceSimulations.computeIfAbsent(url,
                k -> new PriceSimulation(1000 + random.nextInt(2000)));

        BigDecimal currentPrice = simulation.getNextPrice();
        String currency = monitor.getParams().getOrDefault("currency", "TRY");

        log.info("Mock fetched price {} {} for monitor: {}", currentPrice, currency, monitor.getId());

        return new MetricSample("price", currentPrice, currency, Instant.now());
    }

    @Override
    public boolean supports(String sourceType) {
        return "ECOMMERCE_PRODUCT".equalsIgnoreCase(sourceType);
    }

    private static class PriceSimulation {
        private BigDecimal currentPrice;
        private final Random random = new Random();
        private double trend = 0.0;

        public PriceSimulation(double initialPrice) {
            this.currentPrice = BigDecimal.valueOf(initialPrice);
        }

        public BigDecimal getNextPrice() {
            double changePercent = (random.nextGaussian() * 0.02) + (trend * 0.01);

            if (random.nextDouble() < 0.1) {
                trend = (random.nextDouble() - 0.5) * 2;
            }

            BigDecimal change = currentPrice.multiply(BigDecimal.valueOf(changePercent));
            currentPrice = currentPrice.add(change);

            if (currentPrice.compareTo(BigDecimal.valueOf(10)) < 0) {
                currentPrice = BigDecimal.valueOf(10);
            }
            if (currentPrice.compareTo(BigDecimal.valueOf(100000)) > 0) {
                currentPrice = BigDecimal.valueOf(100000);
            }

            return currentPrice.setScale(2, RoundingMode.HALF_UP);
        }
    }
}
