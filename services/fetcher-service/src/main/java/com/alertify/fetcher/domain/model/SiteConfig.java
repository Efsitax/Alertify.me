package com.alertify.fetcher.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SiteConfig {

    /**
     * CSS selectors to try for price extraction (in order of preference)
     */
    private List<String> priceSelectors;

    /**
     * Additional CSS selectors for fallback scenarios
     */
    private List<String> fallbackSelectors;

    /**
     * Custom HTTP headers to send with requests
     */
    private Map<String, String> headers;

    /**
     * Request timeout in milliseconds
     */
    private int timeoutMs;

    /**
     * Whether JavaScript rendering is required
     */
    private boolean requiresJs;

    /**
     * Custom User-Agent string
     */
    private String userAgent;

    /**
     * Wait time after page load (for JS-heavy sites)
     */
    private int waitAfterLoadMs;

    /**
     * Currency code for this site (e.g., "TRY", "USD")
     */
    private String defaultCurrency;

    /**
     * Regex patterns for price extraction
     */
    private List<String> priceRegexPatterns;

    /**
     * Additional configuration specific to this site
     */
    private Map<String, Object> customConfig;

    /**
     * Whether to use Selenium for this site
     */
    private boolean useSelenium;

    /**
     * Whether to parse JSON-LD structured data
     */
    private boolean enableJsonLd;

    /**
     * Whether to parse OpenGraph/meta tags
     */
    private boolean enableMetaTags;

    public static SiteConfig getDefault() {
        return SiteConfig.builder()
                .timeoutMs(10000)
                .requiresJs(false)
                .waitAfterLoadMs(1000)
                .defaultCurrency("TRY")
                .useSelenium(false)
                .enableJsonLd(true)
                .enableMetaTags(true)
                .userAgent("Mozilla/5.0 (compatible; AlertifyBot/1.0)")
                .build();
    }
}