package com.alertify.fetcher.adapter.out.sites.base;

import com.alertify.fetcher.adapter.out.selenium.SeleniumWebDriverService;
import com.alertify.fetcher.domain.model.ExtractionContext;
import com.alertify.fetcher.domain.model.MetricSample;
import com.alertify.fetcher.domain.model.SiteConfig;
import com.alertify.fetcher.domain.port.SiteSpecificFetcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSiteFetcher implements SiteSpecificFetcher {

    private final SeleniumWebDriverService seleniumService;

    @Override
    public boolean supports(String sourceType) {
        return "ECOMMERCE_PRODUCT".equals(sourceType);
    }

    @Override
    public MetricSample fetch(Map<String, String> params) {
        String url = params.get("url");

        if (!isValidUrl(url)) {
            throw new IllegalArgumentException("Invalid URL for " + getSiteName() + ": " + url);
        }

        log.info("Fetching price from {} for URL: {}", getSiteName(), url);

        try {
            String html = fetchHtml(url);
            ExtractionContext context = new ExtractionContext(url, html, params);

            BigDecimal price = extractPrice(context)
                    .orElseThrow(() -> new RuntimeException("Could not extract price from " + getSiteName() + ": " + url));

            String currency = params.getOrDefault("currency", getConfiguration().getDefaultCurrency());

            log.info("Successfully extracted price from {}: {} {}", getSiteName(), price, currency);

            return new MetricSample("price", price, currency, Instant.now());

        } catch (Exception e) {
            log.error("Failed to fetch price from {} for URL {}: {}", getSiteName(), url, e.getMessage());
            throw new RuntimeException("Price fetching failed for " + getSiteName(), e);
        }
    }

    protected abstract Optional<BigDecimal> extractPrice(ExtractionContext context);

    private String fetchHtml(String url) {
        SiteConfig config = getConfiguration();

        if (config.isUseSelenium() || config.isRequiresJs()) {
            return fetchWithSelenium(url);
        } else {
            return fetchWithSimpleHttp(url);
        }
    }

    private String fetchWithSelenium(String url) {
        log.debug("Fetching {} with Selenium", url);

        try {
            String html = seleniumService.fetchPageSource(url);

            // Apply additional wait if configured
            SiteConfig config = getConfiguration();
            if (config.getWaitAfterLoadMs() > 1000) {
                try {
                    Thread.sleep(config.getWaitAfterLoadMs() - 1000); // Selenium already waits ~1s
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }

            return html;
        } catch (Exception e) {
            log.error("Selenium fetch failed for {}: {}", url, e.getMessage());
            throw new RuntimeException("Selenium fetch failed", e);
        }
    }

    private String fetchWithSimpleHttp(String url) {
        log.debug("Fetching {} with simple HTTP", url);

        try {
            URL urlObj = new URL(url);
            HttpURLConnection connection = (HttpURLConnection) urlObj.openConnection();

            SiteConfig config = getConfiguration();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(config.getTimeoutMs());
            connection.setReadTimeout(config.getTimeoutMs());

            String userAgent = config.getUserAgent() != null ?
                    config.getUserAgent() :
                    "Mozilla/5.0 (compatible; AlertifyBot/1.0)";
            connection.setRequestProperty("User-Agent", userAgent);

            if (config.getHeaders() != null) {
                config.getHeaders().forEach(connection::setRequestProperty);
            }

            connection.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8");
            connection.setRequestProperty("Accept-Language", "en-US,en;q=0.5");
            connection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            connection.setRequestProperty("Connection", "keep-alive");
            connection.setRequestProperty("Upgrade-Insecure-Requests", "1");

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 300) {
                StringBuilder content = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(connection.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }

                log.debug("Successfully fetched {} characters from {}", content.length(), url);
                return content.toString();

            } else {
                throw new RuntimeException("HTTP error " + responseCode + " for URL: " + url);
            }

        } catch (Exception e) {
            log.error("Simple HTTP fetch failed for {}: {}", url, e.getMessage());
            throw new RuntimeException("HTTP fetch failed", e);
        }
    }

    protected Optional<BigDecimal> trySelectorsExtraction(ExtractionContext context) {
        try {
            org.jsoup.nodes.Document doc = org.jsoup.Jsoup.parse(context.html());
            SiteConfig config = getConfiguration();

            if (config.getPriceSelectors() != null) {
                for (String selector : config.getPriceSelectors()) {
                    Optional<BigDecimal> price = trySelector(doc, selector);
                    if (price.isPresent()) {
                        log.debug("Price extracted with selector '{}': {}", selector, price.get());
                        return price;
                    }
                }
            }

            if (config.getFallbackSelectors() != null) {
                for (String selector : config.getFallbackSelectors()) {
                    Optional<BigDecimal> price = trySelector(doc, selector);
                    if (price.isPresent()) {
                        log.debug("Price extracted with fallback selector '{}': {}", selector, price.get());
                        return price;
                    }
                }
            }

            return Optional.empty();

        } catch (Exception e) {
            log.warn("Error during CSS selector extraction for {}: {}", getSiteName(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> trySelector(org.jsoup.nodes.Document doc, String selector) {
        org.jsoup.nodes.Element element = doc.selectFirst(selector);
        if (element != null) {
            String priceText = element.text();
            Optional<BigDecimal> price = parsePriceString(priceText);
            if (price.isPresent()) {
                return price;
            }

            String dataPrice = element.attr("data-price");
            if (!dataPrice.isEmpty()) {
                return parsePriceString(dataPrice);
            }
        }
        return Optional.empty();
    }

    protected Optional<BigDecimal> parsePriceString(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return Optional.empty();
        }

        SiteConfig config = getConfiguration();

        if (config.getPriceRegexPatterns() != null) {
            for (String pattern : config.getPriceRegexPatterns()) {
                Optional<BigDecimal> price = tryParseWithPattern(priceText, pattern);
                if (price.isPresent()) {
                    return price;
                }
            }
        }

        return tryDefaultPricePatterns(priceText);
    }

    private Optional<BigDecimal> tryParseWithPattern(String text, String pattern) {
        try {
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(text);

            if (matcher.find()) {
                String priceMatch = matcher.group(1);
                String normalized = priceMatch
                        .replace(".", "")
                        .replace(",", ".");
                return Optional.of(new BigDecimal(normalized));
            }
        } catch (Exception e) {
            log.debug("Failed to parse with pattern '{}': {}", pattern, e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> tryDefaultPricePatterns(String priceText) {
        String[] patterns = {
                "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                "([0-9]{1,3}(?:\\,[0-9]{3})*\\.[0-9]{2})",
                "([0-9]+,[0-9]{2})",
                "([0-9]+\\.[0-9]{2})",
                "([0-9]+)"
        };

        for (String pattern : patterns) {
            Optional<BigDecimal> price = tryParseWithPattern(priceText, pattern);
            if (price.isPresent()) {
                return price;
            }
        }

        return Optional.empty();
    }

    protected String extractDomain(String url) {
        try {
            java.net.URI uri = new java.net.URI(url);
            String host = uri.getHost();
            return host != null ? host.toLowerCase() : "";
        } catch (Exception e) {
            return "";
        }
    }
}