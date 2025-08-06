package com.alertify.worker.adapter.out.http;

import com.alertify.worker.config.FetcherConfigurationProperties;
import com.alertify.worker.domain.entity.Monitor;
import com.alertify.worker.domain.exception.ClientException;
import com.alertify.worker.domain.model.MetricSample;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class FetcherServiceClient {

    private final WebClient webClient;
    private final FetcherConfigurationProperties config;
    private final Random random = new Random();
    private final Map<String, PriceSimulation> priceSimulations = new ConcurrentHashMap<>();

    private final Counter fetchSuccessCounter;
    private final Counter fetchFailureCounter;
    private final Timer fetchTimer;

    public FetcherServiceClient(@Value("${services.fetcher-service.url}") String fetcherServiceUrl,
                                FetcherConfigurationProperties config,
                                MeterRegistry meterRegistry) {
        this.config = config;
        this.webClient = WebClient.builder()
                .baseUrl(fetcherServiceUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();

        this.fetchSuccessCounter = Counter.builder("worker.fetcher.requests")
                .tag("result", "success")
                .register(meterRegistry);
        this.fetchFailureCounter = Counter.builder("worker.fetcher.requests")
                .tag("result", "failure")
                .register(meterRegistry);
        this.fetchTimer = Timer.builder("worker.fetcher.duration")
                .register(meterRegistry);
    }

    public MetricSample fetchMetric(Monitor monitor) {
        try {
            return fetchTimer.recordCallable(() -> {
                try {
                    MetricSample result = "http".equalsIgnoreCase(config.getMode())
                            ? fetchViaHttp(monitor)
                            : fetchViaMock(monitor);

                    fetchSuccessCounter.increment();
                    log.debug("Successfully fetched metric for monitor {}: {} {}",
                            monitor.getId(), result.value(), result.unit());
                    return result;

                } catch (Exception e) {
                    fetchFailureCounter.increment();
                    log.error("Failed to fetch metric for monitor {}: {}", monitor.getId(), e.getMessage());
                    throw new RuntimeException("Failed to fetch metric for monitor " + monitor.getId(), e);
                }
            });
        } catch (Exception e) {
            if (e.getCause() instanceof ClientException) {
                throw (ClientException) e.getCause();
            }
            throw new ClientException("Failed to fetch metric for monitor " + monitor.getId(), e);
        }
    }

    private MetricSample fetchViaHttp(Monitor monitor) {
        log.debug("Fetching metric for monitor {} via HTTP", monitor.getId());

        try {
            FetchRequest request = new FetchRequest(monitor.getSourceType(), monitor.getParams());

            MetricSample sample = webClient.post()
                    .uri("/api/fetch")
                    .bodyValue(request)
                    .retrieve()
                    .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                            response -> response.bodyToMono(String.class)
                                    .map(body -> new RuntimeException("HTTP error: " + response.statusCode() + " - " + body)))
                    .bodyToMono(MetricSample.class)
                    .timeout(Duration.ofMillis(config.getHttp().getTimeout()))
                    .block();

            if (sample == null) {
                throw new ClientException("Received null response from fetcher service", new RuntimeException("Null response"));
            }

            return sample;

        } catch (WebClientResponseException e) {
            throw new ClientException("HTTP error " + e.getStatusCode() + ": " + e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            throw new ClientException("Network error while fetching metric", e);
        }
    }

    private MetricSample fetchViaMock(Monitor monitor) {
        log.debug("Fetching mock metric for monitor: {}", monitor.getId());

        if (random.nextDouble() < config.getMock().getFailureRate()) {
            throw new ClientException("Simulated network error for monitor: " + monitor.getId(), new RuntimeException("Mock failure"));
        }

        simulateNetworkDelay();

        String monitorKey = monitor.getId().toString();
        PriceSimulation simulation = priceSimulations.computeIfAbsent(monitorKey,
                k -> createPriceSimulation(monitor));

        BigDecimal currentPrice = simulation.getNextPrice();
        String currency = monitor.getParams().getOrDefault("currency", "TRY");

        log.info("Mock fetched price {} {} for monitor {}", currentPrice, currency, monitor.getId());

        return new MetricSample("price", currentPrice, currency, Instant.now());
    }

    private void simulateNetworkDelay() {
        try {
            int delay = config.getMock().getDelayMin() +
                    random.nextInt(config.getMock().getDelayMax() - config.getMock().getDelayMin());
            Thread.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ClientException("Interrupted while simulating network delay", e);
        }
    }

    private PriceSimulation createPriceSimulation(@SuppressWarnings("unused") Monitor monitor) {
        double basePrice = config.getMock().getBasePriceMin() +
                random.nextDouble() * (config.getMock().getBasePriceMax() - config.getMock().getBasePriceMin());
        return new PriceSimulation(basePrice, config.getMock().getPriceVolatility());
    }

    private static class PriceSimulation {
        private BigDecimal currentPrice;
        private final Random random = new Random();
        private final double volatility;
        private double trend = 0.0;

        public PriceSimulation(double initialPrice, double volatility) {
            this.currentPrice = BigDecimal.valueOf(initialPrice);
            this.volatility = volatility;
        }

        public BigDecimal getNextPrice() {
            double changePercent = (random.nextGaussian() * volatility) + (trend * 0.01);

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

    public record FetchRequest(String sourceType, Map<String, String> params) {}
}