package com.alertify.fetcher.adapter.out.sites.hepsiburada;

import com.alertify.fetcher.adapter.out.selenium.SeleniumWebDriverService;
import com.alertify.fetcher.adapter.out.sites.base.AbstractSiteFetcher;
import com.alertify.fetcher.domain.model.ExtractionContext;
import com.alertify.fetcher.domain.model.SiteConfig;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HepsiBurada-specific fetcher implementation with Selenium support
 * Handles price extraction from HepsiBurada.com product pages
 */
@Slf4j
@Component
public class HepsiBuradaFetcher extends AbstractSiteFetcher {

    private static final List<String> SUPPORTED_DOMAINS = List.of(
            "hepsiburada.com",
            "www.hepsiburada.com"
    );

    private static final SiteConfig HEPSIBURADA_CONFIG = SiteConfig.builder()
            .priceSelectors(List.of(
                    "#offering-price",                    // Main price selector
                    ".price-value",                       // Alternative price
                    ".product-price .price-value",        // Nested price
                    "[data-test-id='price-current-price']", // Test ID
                    ".notranslate",                       // Sometimes price is in notranslate
                    "#priceContainer .price-value",       // Container-based
                    ".product-price-text",                // Alternative text selector
                    "[data-price-text]",                  // Data attribute
                    ".offering-price-info .price-value"   // Detailed price container
            ))
            .fallbackSelectors(List.of(
                    ".price",
                    ".current-price",
                    ".sale-price",
                    "[data-price]",
                    ".product-detail-price",
                    ".product-price-container"
            ))
            .priceRegexPatterns(List.of(
                    "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})", // Turkish format: 1.234,56
                    "([0-9]+,[0-9]{2})",                      // Simple comma: 1234,56
                    "([0-9]+)",                               // Integer: 1234
                    "([0-9]+\\.[0-9]{2})"                     // Dot format: 1234.56
            ))
            .headers(Map.of(
                    "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
                    "Accept-Language", "tr-TR,tr;q=0.9,en-US;q=0.8,en;q=0.7",
                    "Accept-Encoding", "gzip, deflate, br",
                    "Connection", "keep-alive",
                    "Upgrade-Insecure-Requests", "1",
                    "Sec-Fetch-Dest", "document",
                    "Sec-Fetch-Mode", "navigate",
                    "Sec-Fetch-Site", "none",
                    "Sec-Fetch-User", "?1",
                    "Cache-Control", "max-age=0"
            ))
            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .timeoutMs(20000)
            .requiresJs(true)   // ✅ Changed to true - Use Selenium
            .useSelenium(true)  // ✅ Changed to true - Force Selenium
            .waitAfterLoadMs(4000) // Increased wait time for dynamic content
            .defaultCurrency("TRY")
            .enableJsonLd(true)
            .enableMetaTags(true)
            .build();

    public HepsiBuradaFetcher(SeleniumWebDriverService seleniumService) {
        super(seleniumService);
    }

    @Override
    public boolean supportsDomain(String domain) {
        return SUPPORTED_DOMAINS.stream()
                .anyMatch(supportedDomain -> domain.toLowerCase().contains(supportedDomain));
    }

    @Override
    public String getSiteName() {
        return "HepsiBurada";
    }

    @Override
    public List<String> getSupportedDomains() {
        return SUPPORTED_DOMAINS;
    }

    @Override
    public SiteConfig getConfiguration() {
        return HEPSIBURADA_CONFIG;
    }

    @Override
    public int getPriority() {
        return 20; // High priority for HepsiBurada
    }

    @Override
    protected Optional<BigDecimal> extractPrice(ExtractionContext context) {
        log.info("Extracting price from HepsiBurada using Selenium-based specialized logic");

        // Strategy 1: Try JSON-LD structured data
        Optional<BigDecimal> jsonLdPrice = extractFromJsonLd(context);
        if (jsonLdPrice.isPresent()) {
            log.info("Price extracted from JSON-LD: {}", jsonLdPrice.get());
            return jsonLdPrice;
        }

        // Strategy 2: Try HepsiBurada-specific selectors
        Optional<BigDecimal> selectorPrice = extractWithHepsiBuradaSelectors(context);
        if (selectorPrice.isPresent()) {
            log.info("Price extracted with HepsiBurada selectors: {}", selectorPrice.get());
            return selectorPrice;
        }

        // Strategy 3: Try data attributes
        Optional<BigDecimal> dataPrice = extractFromDataAttributes(context);
        if (dataPrice.isPresent()) {
            log.info("Price extracted from data attributes: {}", dataPrice.get());
            return dataPrice;
        }

        // Strategy 4: Try meta tags
        Optional<BigDecimal> metaPrice = extractFromMetaTags(context);
        if (metaPrice.isPresent()) {
            log.info("Price extracted from meta tags: {}", metaPrice.get());
            return metaPrice;
        }

        // Strategy 5: Fallback to generic selectors
        Optional<BigDecimal> fallbackPrice = trySelectorsExtraction(context);
        if (fallbackPrice.isPresent()) {
            log.info("Price extracted with fallback selectors: {}", fallbackPrice.get());
            return fallbackPrice;
        }

        // Strategy 6: Aggressive text scanning
        Optional<BigDecimal> textPrice = extractFromPageText(context);
        if (textPrice.isPresent()) {
            log.info("Price extracted from page text: {}", textPrice.get());
            return textPrice;
        }

        log.warn("Could not extract price from HepsiBurada page: {}", context.url());
        return Optional.empty();
    }

    /**
     * Extract price from JSON-LD structured data
     */
    private Optional<BigDecimal> extractFromJsonLd(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());
            Element jsonLdScript = doc.selectFirst("script[type='application/ld+json']");

            if (jsonLdScript != null) {
                String jsonContent = jsonLdScript.html();
                return parseJsonLdForPrice(jsonContent);
            }
        } catch (Exception e) {
            log.debug("JSON-LD extraction failed for HepsiBurada: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Parse JSON-LD content for price (enhanced regex-based approach)
     */
    private Optional<BigDecimal> parseJsonLdForPrice(String jsonContent) {
        try {
            if (jsonContent.contains("\"@type\":\"Product\"") || jsonContent.contains("\"@type\":\"Offer\"")) {
                // Enhanced regex patterns for HepsiBurada JSON-LD
                String[] pricePatterns = {
                        "\"price\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]{2})?)\"?",
                        "\"priceValue\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]{2})?)\"?",
                        "\"amount\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]{2})?)\"?",
                        "\"lowPrice\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]{2})?)\"?",
                        "\"offers\"[^}]*\"price\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]{2})?)\"?"
                };

                for (String pattern : pricePatterns) {
                    java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
                    java.util.regex.Matcher matcher = regex.matcher(jsonContent);

                    if (matcher.find()) {
                        try {
                            String priceStr = matcher.group(1);
                            BigDecimal price = new BigDecimal(priceStr);
                            log.debug("JSON-LD price found with pattern '{}': {}", pattern, price);
                            return Optional.of(price);
                        } catch (NumberFormatException e) {
                            log.debug("Failed to parse JSON-LD price: {}", matcher.group(1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse JSON-LD content: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Extract price using HepsiBurada-specific selectors
     */
    private Optional<BigDecimal> extractWithHepsiBuradaSelectors(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            // Enhanced HepsiBurada selectors based on latest site structure
            String[] hepsiBuradaSelectors = {
                    "#offering-price",                          // Main price
                    "#priceContainer .price-value",             // Container
                    ".product-price .price-value",              // Product price
                    "[data-test-id='price-current-price']",     // Test ID
                    ".price-value.notranslate",                 // Notranslate price
                    ".price-value:not(.old-price)",             // Current price (not old)
                    "#productPrice .price-value",               // Product price section
                    ".offering-price-info .price-value",        // Offering info
                    ".product-detail-price .price-value",       // Detail page
                    "[data-price-text]",                        // Data attribute
                    ".product-price-text",                      // Text container
                    ".current-price-value",                     // Current price
                    ".final-price .price-value"                 // Final price
            };

            for (String selector : hepsiBuradaSelectors) {
                Element priceElement = doc.selectFirst(selector);
                if (priceElement != null) {
                    String priceText = priceElement.text();
                    log.debug("Found price element with selector '{}': {}", selector, priceText);

                    Optional<BigDecimal> price = parseHepsiBuradaPrice(priceText);
                    if (price.isPresent()) {
                        return price;
                    }

                    // Also try data attributes on this element
                    String dataPriceValue = priceElement.attr("data-price");
                    if (!dataPriceValue.isEmpty()) {
                        Optional<BigDecimal> dataPrice = parseHepsiBuradaPrice(dataPriceValue);
                        if (dataPrice.isPresent()) {
                            return dataPrice;
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.debug("HepsiBurada selector extraction failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Parse HepsiBurada-specific price format with enhanced patterns
     */
    private Optional<BigDecimal> parseHepsiBuradaPrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return Optional.empty();
        }

        // Clean the price text (HepsiBurada format)
        String cleaned = priceText
                .replaceAll("TL", "")
                .replaceAll("₺", "")
                .replaceAll("\\s+", "")
                .replaceAll("[^0-9.,]", "") // Remove all non-numeric except . and ,
                .trim();

        log.debug("Cleaned price text: {} -> {}", priceText, cleaned);

        // HepsiBurada price patterns (more comprehensive)
        String[] patterns = {
                "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",  // 1.234,56
                "([0-9]+,[0-9]{2})",                       // 1234,56
                "([0-9]{1,3}(?:\\,[0-9]{3})*\\.[0-9]{2})", // 1,234.56 (less common)
                "([0-9]+\\.[0-9]{2})",                     // 1234.56
                "([0-9]+\\.[0-9]{3})",                     // 1234.500 (sometimes no decimals shown)
                "([0-9]+)"                                 // 1234 (integer)
        };

        for (String pattern : patterns) {
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(cleaned);

            if (matcher.find()) {
                try {
                    String priceMatch = matcher.group(1);
                    log.debug("Price pattern matched: {} with pattern: {}", priceMatch, pattern);

                    // Convert to decimal format
                    String normalized;
                    if (priceMatch.contains(",") && priceMatch.contains(".")) {
                        // Determine which is decimal separator based on position
                        int lastComma = priceMatch.lastIndexOf(",");
                        int lastDot = priceMatch.lastIndexOf(".");

                        if (lastComma > lastDot) {
                            // Comma is decimal: 1.234,56 -> 1234.56
                            normalized = priceMatch.substring(0, lastComma).replace(".", "").replace(",", "") +
                                    "." + priceMatch.substring(lastComma + 1);
                        } else {
                            // Dot is decimal: 1,234.56 -> 1234.56
                            normalized = priceMatch.substring(0, lastDot).replace(",", "") +
                                    "." + priceMatch.substring(lastDot + 1);
                        }
                    } else if (priceMatch.contains(",")) {
                        // Only comma - check if it's decimal separator
                        if (priceMatch.matches(".*,[0-9]{2}$")) {
                            // Decimal separator: 1234,56 -> 1234.56
                            normalized = priceMatch.replace(",", ".");
                        } else {
                            // Thousands separator: 1,234 -> 1234
                            normalized = priceMatch.replace(",", "");
                        }
                    } else {
                        // No comma, keep as is
                        normalized = priceMatch;
                    }

                    BigDecimal price = new BigDecimal(normalized);
                    log.debug("Parsed HepsiBurada price: {} -> {}", priceText, price);
                    return Optional.of(price);

                } catch (NumberFormatException e) {
                    log.debug("Failed to parse price match '{}': {}", matcher.group(1), e.getMessage());
                }
            }
        }

        return Optional.empty();
    }

    /**
     * Extract price from data attributes
     */
    private Optional<BigDecimal> extractFromDataAttributes(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            // Enhanced data attribute selectors for HepsiBurada
            String[] dataSelectors = {
                    "[data-price]",
                    "[data-product-price]",
                    "[data-current-price]",
                    "[data-offering-price]",
                    "[data-price-value]",
                    "[data-test-id*='price'] [data-value]",
                    "[data-price-text]"
            };

            for (String selector : dataSelectors) {
                Element element = doc.selectFirst(selector);
                if (element != null) {
                    String[] attributesToTry = {
                            "data-price", "data-product-price", "data-current-price",
                            "data-offering-price", "data-price-value", "data-value",
                            "data-price-text"
                    };

                    for (String attr : attributesToTry) {
                        String dataPrice = element.attr(attr);
                        if (!dataPrice.isEmpty()) {
                            Optional<BigDecimal> price = parsePriceString(dataPrice);
                            if (price.isPresent()) {
                                log.debug("Price extracted from data attribute '{}' in '{}': {}",
                                        attr, selector, price.get());
                                return price;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Data attribute extraction failed for HepsiBurada: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Extract price from meta tags
     */
    private Optional<BigDecimal> extractFromMetaTags(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            String[] metaSelectors = {
                    "meta[property='product:price:amount']",
                    "meta[property='og:price:amount']",
                    "meta[name='price']",
                    "meta[property='price']",
                    "meta[name='product:price']"
            };

            for (String selector : metaSelectors) {
                Element metaElement = doc.selectFirst(selector);
                if (metaElement != null) {
                    String content = metaElement.attr("content");
                    if (!content.isEmpty()) {
                        Optional<BigDecimal> price = parsePriceString(content);
                        if (price.isPresent()) {
                            log.debug("Price extracted from meta tag '{}': {}", selector, price.get());
                            return price;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Meta tag extraction failed for HepsiBurada: {}", e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Extract price from page text as last resort
     */
    private Optional<BigDecimal> extractFromPageText(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            // Remove scripts and styles
            doc.select("script, style, noscript").remove();

            String pageText = doc.text();

            // Look for Turkish price patterns in text
            String[] textPatterns = {
                    "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})\\s*TL",
                    "([0-9]+,[0-9]{2})\\s*TL",
                    "₺\\s*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                    "₺\\s*([0-9]+,[0-9]{2})",
                    "Fiyat[:\\s]*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                    "Fiyat[:\\s]*([0-9]+,[0-9]{2})"
            };

            for (String pattern : textPatterns) {
                java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher matcher = regex.matcher(pageText);

                if (matcher.find()) {
                    String priceStr = matcher.group(1);
                    Optional<BigDecimal> price = parseHepsiBuradaPrice(priceStr);
                    if (price.isPresent()) {
                        // Sanity check
                        if (price.get().compareTo(BigDecimal.valueOf(1)) >= 0 &&
                                price.get().compareTo(BigDecimal.valueOf(1000000)) <= 0) {
                            log.debug("Price extracted from text with pattern '{}': {}", pattern, price.get());
                            return price;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Page text extraction failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean isValidUrl(String url) {
        if (!super.isValidUrl(url)) {
            return false;
        }

        // Enhanced HepsiBurada URL validation
        return url.contains("-p-") ||
                url.contains("/product/") ||
                url.matches(".*hepsiburada\\.com.*\\d+.*") ||
                url.matches(".*hepsiburada\\.com.*p-.*");
    }
}