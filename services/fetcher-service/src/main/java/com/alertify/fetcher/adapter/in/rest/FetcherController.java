package com.alertify.fetcher.adapter.in.rest;

import com.alertify.fetcher.domain.model.MetricSample;
import com.alertify.fetcher.domain.port.MetricFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/fetch")
@RequiredArgsConstructor
public class FetcherController {

    private final List<MetricFetcher> metricFetchers;

    @PostMapping
    public ResponseEntity<MetricSample> fetchMetric(@RequestBody FetchRequest request) {
        log.info("Received fetch request for sourceType: {}", request.sourceType());

        MetricFetcher fetcher = metricFetchers.stream()
                .filter(f -> f.supports(request.sourceType()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "No fetcher found for source type: " + request.sourceType()));

        MetricSample sample = fetcher.fetch(request.params());

        log.info("Successfully fetched metric: {} = {} {}",
                sample.metric(), sample.value(), sample.unit());

        return ResponseEntity.ok(sample);
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "fetcher-service"
        ));
    }

    @GetMapping("/supported-sources")
    public ResponseEntity<List<String>> getSupportedSources() {
        List<String> supportedSources = List.of("ECOMMERCE_PRODUCT");
        return ResponseEntity.ok(supportedSources);
    }

    public record FetchRequest(
            String sourceType,
            Map<String, String> params
    ) {}
}