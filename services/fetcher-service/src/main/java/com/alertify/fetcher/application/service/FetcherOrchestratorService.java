package com.alertify.fetcher.application.service;

import com.alertify.fetcher.domain.model.MetricSample;
import com.alertify.fetcher.domain.port.MetricFetcher;
import com.alertify.fetcher.domain.port.SiteSpecificFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@Primary
@RequiredArgsConstructor
public class FetcherOrchestratorService implements MetricFetcher {

    private final List<SiteSpecificFetcher> siteSpecificFetchers;

    @Override
    public boolean supports(String sourceType) {
        return "ECOMMERCE_PRODUCT".equals(sourceType);
    }

    @Override
    public MetricSample fetch(Map<String, String> params) {
        String url = params.get("url");

        if (url == null || url.trim().isEmpty()) {
            throw new IllegalArgumentException("URL parameter is required");
        }

        log.info("Orchestrating price fetch for URL: {}", url);

        try {
            String domain = extractDomain(url);
            log.debug("Extracted domain: {}", domain);

            Optional<SiteSpecificFetcher> bestFetcher = findBestFetcher(domain, url);

            if (bestFetcher.isPresent()) {
                SiteSpecificFetcher fetcher = bestFetcher.get();
                log.info("Using {} fetcher for domain: {}", fetcher.getSiteName(), domain);

                try {
                    MetricSample result = fetcher.fetch(params);
                    log.info("Successfully fetched price using {} fetcher: {} {}",
                            fetcher.getSiteName(), result.value(), result.unit());
                    return result;

                } catch (Exception e) {
                    log.warn("Primary fetcher {} failed for {}: {}",
                            fetcher.getSiteName(), url, e.getMessage());

                    return tryFallbackFetchers(params, domain, fetcher);
                }
            } else {
                throw new RuntimeException("No suitable fetcher found for URL: " + url);
            }

        } catch (Exception e) {
            log.error("Failed to fetch price for URL {}: {}", url, e.getMessage());
            throw new RuntimeException("Price fetching failed for URL: " + url, e);
        }
    }

    private Optional<SiteSpecificFetcher> findBestFetcher(String domain, String url) {
        return siteSpecificFetchers.stream()
                .filter(fetcher -> fetcher.supportsDomain(domain))
                .filter(fetcher -> fetcher.isValidUrl(url))
                .min(Comparator.comparing(SiteSpecificFetcher::getPriority));
    }

    private MetricSample tryFallbackFetchers(Map<String, String> params, String domain,
                                             SiteSpecificFetcher failedFetcher) {
        String url = params.get("url");

        log.info("Trying fallback fetchers for URL: {}", url);

        List<SiteSpecificFetcher> fallbackFetchers = siteSpecificFetchers.stream()
                .filter(fetcher -> !fetcher.equals(failedFetcher))
                .filter(fetcher -> fetcher.supportsDomain(domain) || fetcher.getSiteName().contains("Generic"))
                .sorted(Comparator.comparing(SiteSpecificFetcher::getPriority))
                .toList();

        Exception lastException = null;

        for (SiteSpecificFetcher fetcher : fallbackFetchers) {
            try {
                log.debug("Trying fallback fetcher: {}", fetcher.getSiteName());
                MetricSample result = fetcher.fetch(params);
                log.info("Fallback fetcher {} succeeded: {} {}",
                        fetcher.getSiteName(), result.value(), result.unit());
                return result;

            } catch (Exception e) {
                log.debug("Fallback fetcher {} failed: {}", fetcher.getSiteName(), e.getMessage());
                lastException = e;
            }
        }

        throw new RuntimeException("All fetchers failed for URL: " + url, lastException);
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            if (host != null) {
                if (host.startsWith("www.")) {
                    host = host.substring(4);
                }
                return host.toLowerCase();
            }
            return "";
        } catch (Exception e) {
            log.warn("Failed to extract domain from URL {}: {}", url, e.getMessage());
            return "";
        }
    }

    public List<FetcherInfo> getAvailableFetchers() {
        return siteSpecificFetchers.stream()
                .map(fetcher -> new FetcherInfo(
                        fetcher.getSiteName(),
                        fetcher.getSupportedDomains(),
                        fetcher.getPriority(),
                        fetcher.requiresJavaScript(),
                        fetcher.getConfiguration().getDefaultCurrency(),
                        determineFetchMethod(fetcher)
                ))
                .sorted(Comparator.comparing(FetcherInfo::priority))
                .toList();
    }

    private String determineFetchMethod(SiteSpecificFetcher fetcher) {
        var config = fetcher.getConfiguration();
        if (config.isUseSelenium() || config.isRequiresJs()) {
            return "Selenium WebDriver";
        } else {
            return "Simple HTTP";
        }
    }

    public Optional<String> getBestFetcherName(String url) {
        try {
            String domain = extractDomain(url);
            return findBestFetcher(domain, url)
                    .map(SiteSpecificFetcher::getSiteName);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public boolean isUrlSupported(String url) {
        try {
            String domain = extractDomain(url);
            return siteSpecificFetchers.stream()
                    .anyMatch(fetcher -> fetcher.supportsDomain(domain) && fetcher.isValidUrl(url));
        } catch (Exception e) {
            return false;
        }
    }

    public FetcherAnalysis analyzeFetcher(String url) {
        try {
            String domain = extractDomain(url);
            Optional<SiteSpecificFetcher> bestFetcher = findBestFetcher(domain, url);

            List<String> allSupportingFetchers = siteSpecificFetchers.stream()
                    .filter(fetcher -> fetcher.supportsDomain(domain))
                    .map(SiteSpecificFetcher::getSiteName)
                    .toList();

            return new FetcherAnalysis(
                    url,
                    domain,
                    bestFetcher.map(SiteSpecificFetcher::getSiteName).orElse("None"),
                    allSupportingFetchers,
                    bestFetcher.map(this::determineFetchMethod).orElse("Unknown"),
                    isUrlSupported(url)
            );
        } catch (Exception e) {
            return new FetcherAnalysis(url, "", "Error", List.of(), "Unknown", false);
        }
    }

    public FetcherSystemStats getSystemStats() {
        int totalFetchers = siteSpecificFetchers.size();
        long seleniumFetchers = siteSpecificFetchers.stream()
                .mapToLong(f -> f.getConfiguration().isUseSelenium() ? 1 : 0)
                .sum();
        long httpFetchers = totalFetchers - seleniumFetchers;

        return new FetcherSystemStats(
                totalFetchers,
                (int) seleniumFetchers,
                (int) httpFetchers,
                siteSpecificFetchers.stream()
                        .flatMap(f -> f.getSupportedDomains().stream())
                        .filter(domain -> !"*".equals(domain))
                        .distinct()
                        .count()
        );
    }

    public record FetcherInfo(
            String name,
            List<String> supportedDomains,
            int priority,
            boolean requiresJavaScript,
            String defaultCurrency,
            String fetchMethod
    ) {}

    public record FetcherAnalysis(
            String url,
            String domain,
            String bestFetcher,
            List<String> allSupportingFetchers,
            String fetchMethod,
            boolean supported
    ) {}

    public record FetcherSystemStats(
            int totalFetchers,
            int seleniumFetchers,
            int httpFetchers,
            long supportedDomains
    ) {}
}