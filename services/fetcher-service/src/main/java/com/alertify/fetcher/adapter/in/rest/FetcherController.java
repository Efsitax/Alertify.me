package com.alertify.fetcher.adapter.in.rest;

import com.alertify.fetcher.application.service.FetcherOrchestratorService;
import com.alertify.fetcher.domain.model.MetricSample;
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

    private final FetcherOrchestratorService orchestratorService;

    @PostMapping
    public ResponseEntity<MetricSample> fetchMetric(@RequestBody FetchRequest request) {
        log.info("Received fetch request for sourceType: {} and URL: {}",
                request.sourceType(), request.params().get("url"));

        try {
            if (!orchestratorService.supports(request.sourceType())) {
                return ResponseEntity.badRequest().build();
            }

            MetricSample sample = orchestratorService.fetch(request.params());

            log.info("Successfully fetched metric: {} = {} {}",
                    sample.metric(), sample.value(), sample.unit());

            return ResponseEntity.ok(sample);

        } catch (IllegalArgumentException e) {
            log.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity.badRequest().build();

        } catch (Exception e) {
            log.error("Failed to fetch metric: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "fetcher-service",
                "version", "2.0.0"
        ));
    }

    @GetMapping("/supported-sources")
    public ResponseEntity<List<String>> getSupportedSources() {
        List<String> supportedSources = List.of("ECOMMERCE_PRODUCT");
        return ResponseEntity.ok(supportedSources);
    }

    @GetMapping("/fetchers")
    public ResponseEntity<List<FetcherOrchestratorService.FetcherInfo>> getAvailableFetchers() {
        List<FetcherOrchestratorService.FetcherInfo> fetchers = orchestratorService.getAvailableFetchers();
        return ResponseEntity.ok(fetchers);
    }

    @GetMapping("/best-fetcher")
    public ResponseEntity<Map<String, String>> getBestFetcher(@RequestParam String url) {
        try {
            String fetcherName = orchestratorService.getBestFetcherName(url)
                    .orElse("No suitable fetcher found");

            boolean supported = orchestratorService.isUrlSupported(url);

            return ResponseEntity.ok(Map.of(
                    "url", url,
                    "bestFetcher", fetcherName,
                    "supported", String.valueOf(supported)
            ));
        } catch (Exception e) {
            log.error("Error determining best fetcher for URL {}: {}", url, e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid URL or fetcher determination failed"
            ));
        }
    }

    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateUrl(@RequestBody Map<String, String> request) {
        String url = request.get("url");

        if (url == null || url.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of(
                    "valid", false,
                    "reason", "URL is required"
            ));
        }

        try {
            boolean supported = orchestratorService.isUrlSupported(url);
            String bestFetcher = orchestratorService.getBestFetcherName(url).orElse("None");

            return ResponseEntity.ok(Map.of(
                    "valid", supported,
                    "url", url,
                    "bestFetcher", bestFetcher,
                    "supported", supported
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "valid", false,
                    "url", url,
                    "reason", "URL validation failed: " + e.getMessage()
            ));
        }
    }

    public record FetchRequest(
            String sourceType,
            Map<String, String> params
    ) {}
}