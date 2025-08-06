package com.alertify.worker.adapter.out.http;

import com.alertify.worker.domain.entity.Monitor;
import com.alertify.worker.domain.exception.ClientException;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class MonitorServiceClient {

    private final WebClient webClient;

    // Metrics
    private final Counter fetchSuccessCounter;
    private final Counter fetchFailureCounter;
    private final Timer fetchTimer;

    public MonitorServiceClient(@Value("${services.monitor-service.url}") String monitorServiceUrl,
                                MeterRegistry meterRegistry) {
        this.webClient = WebClient.builder()
                .baseUrl(monitorServiceUrl)
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(1024 * 1024))
                .build();

        // Initialize metrics
        this.fetchSuccessCounter = Counter.builder("worker.monitor_service.requests")
                .tag("result", "success")
                .register(meterRegistry);
        this.fetchFailureCounter = Counter.builder("worker.monitor_service.requests")
                .tag("result", "failure")
                .register(meterRegistry);
        this.fetchTimer = Timer.builder("worker.monitor_service.duration")
                .register(meterRegistry);
    }

    public List<Monitor> fetchActiveMonitors() {
        try {
            return fetchTimer.recordCallable(() -> {
                try {
                    log.debug("Fetching active monitors from monitor service");

                    Monitor[] monitors = webClient.get()
                            .uri("/api/monitors?status=ACTIVE")
                            .retrieve()
                            .onStatus(status -> status.is4xxClientError() || status.is5xxServerError(),
                                    response -> response.bodyToMono(String.class)
                                            .map(body -> new RuntimeException("HTTP error: " + response.statusCode() + " - " + body)))
                            .bodyToMono(Monitor[].class)
                            .timeout(Duration.ofSeconds(10))
                            .block();

                    List<Monitor> result = monitors != null ? Arrays.asList(monitors) : List.of();

                    fetchSuccessCounter.increment();
                    log.info("Successfully fetched {} active monitors from monitor service", result.size());

                    return result;

                } catch (WebClientResponseException e) {
                    fetchFailureCounter.increment();
                    log.error("HTTP error while fetching active monitors: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
                    throw new RuntimeException("HTTP error while fetching active monitors: " + e.getStatusCode(), e);
                } catch (Exception e) {
                    fetchFailureCounter.increment();
                    log.error("Error fetching active monitors: {}", e.getMessage(), e);
                    throw new RuntimeException("Error fetching active monitors", e);
                }
            });
        } catch (Exception e) {
            if (e.getCause() instanceof ClientException) {
                throw (ClientException) e.getCause();
            }
            throw new ClientException("Error fetching active monitors", e);
        }
    }

    @SuppressWarnings("unused")
    public boolean isHealthy() {
        try {
            webClient.get()
                    .uri("/actuator/health")
                    .retrieve()
                    .bodyToMono(String.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();
            return true;
        } catch (Exception e) {
            log.warn("Monitor service health check failed: {}", e.getMessage());
            return false;
        }
    }
}