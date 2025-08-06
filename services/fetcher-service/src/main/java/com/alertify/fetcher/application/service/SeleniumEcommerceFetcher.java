package com.alertify.fetcher.application.service;

import com.alertify.fetcher.adapter.out.selenium.SeleniumWebDriverService;
import com.alertify.fetcher.domain.model.ExtractionContext;
import com.alertify.fetcher.domain.model.MetricSample;
import com.alertify.fetcher.domain.port.MetricFetcher;
import com.alertify.fetcher.domain.port.PriceExtractionStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class SeleniumEcommerceFetcher implements MetricFetcher {

    private final SeleniumWebDriverService seleniumService;
    private final List<PriceExtractionStrategy> extractionStrategies;

    @Override
    public boolean supports(String sourceType) {
        return "ECOMMERCE_PRODUCT".equals(sourceType);
    }

    @Override
    public MetricSample fetch(java.util.Map<String, String> params) {
        String url = params.get("url");
        if (url == null || url.isEmpty()) {
            throw new IllegalArgumentException("URL parameter is required");
        }

        try {
            log.info("Fetching price with Selenium from URL: {}", url);

            String html = seleniumService.fetchPageSource(url);

            ExtractionContext context = new ExtractionContext(url, html, params);

            BigDecimal price = extractionStrategies.stream()
                    .sorted(Comparator.comparing(PriceExtractionStrategy::getPriority))
                    .map(strategy -> {
                        try {
                            return strategy.extractPrice(context);
                        } catch (Exception e) {
                            log.debug("Strategy {} failed: {}", strategy.getClass().getSimpleName(), e.getMessage());
                            return Optional.<BigDecimal>empty();
                        }
                    })
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Could not extract price from URL: " + url));

            String currency = params.getOrDefault("currency", "TRY");
            log.info("Successfully extracted price: {} {} from {}", price, currency, url);

            return new MetricSample("price", price, currency, Instant.now());

        } catch (Exception e) {
            log.error("Failed to fetch price from {}: {}", url, e.getMessage());
            throw new RuntimeException("Price fetching failed for URL: " + url, e);
        }
    }
}