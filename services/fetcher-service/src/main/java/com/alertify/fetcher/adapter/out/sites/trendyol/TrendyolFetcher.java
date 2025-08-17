package com.alertify.fetcher.adapter.out.sites.trendyol;

import com.alertify.fetcher.adapter.out.selenium.SeleniumWebDriverService;
import com.alertify.fetcher.adapter.out.sites.base.AbstractSiteFetcher;
import com.alertify.fetcher.domain.model.ExtractionContext;
import com.alertify.fetcher.domain.model.SiteConfig;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class TrendyolFetcher extends AbstractSiteFetcher {

    private static final List<String> SUPPORTED_DOMAINS = List.of(
            "trendyol.com",
            "www.trendyol.com"
    );

    private static final SiteConfig TRENDYOL_CONFIG = SiteConfig.builder()
            .priceSelectors(List.of(
                    ".prc-dsc",
                    ".prc-org",
                    ".product-price-container .prc-dsc",
                    ".product-price .prc-dsc",
                    "[data-test-id='price-current-price']"
            ))
            .fallbackSelectors(List.of(
                    ".price",
                    ".product-price",
                    ".current-price",
                    "[data-price]"
            ))
            .priceRegexPatterns(List.of(
                    "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                    "([0-9]+,[0-9]{2})",
                    "([0-9]+)",
                    "([0-9]+\\.[0-9]{2})"
            ))
            .headers(Map.of(
                    "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                    "Accept-Language", "tr-TR,tr;q=0.9,en;q=0.8",
                    "Accept-Encoding", "gzip, deflate",
                    "Connection", "keep-alive",
                    "Upgrade-Insecure-Requests", "1"
            ))
            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .timeoutMs(15000)
            .requiresJs(true)
            .useSelenium(true)
            .waitAfterLoadMs(3000)
            .defaultCurrency("TRY")
            .enableJsonLd(true)
            .enableMetaTags(true)
            .build();

    public TrendyolFetcher(SeleniumWebDriverService seleniumService) {
        super(seleniumService);
    }

    @Override
    public boolean supportsDomain(String domain) {
        return SUPPORTED_DOMAINS.stream()
                .anyMatch(supportedDomain -> domain.toLowerCase().contains(supportedDomain));
    }

    @Override
    public String getSiteName() {
        return "Trendyol";
    }

    @Override
    public List<String> getSupportedDomains() {
        return SUPPORTED_DOMAINS;
    }

    @Override
    public SiteConfig getConfiguration() {
        return TRENDYOL_CONFIG;
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    protected Optional<BigDecimal> extractPrice(ExtractionContext context) {
        log.info("Extracting price from Trendyol using specialized logic");

        Optional<BigDecimal> jsonLdPrice = extractFromJsonLd(context);
        if (jsonLdPrice.isPresent()) {
            log.info("Price extracted from JSON-LD: {}", jsonLdPrice.get());
            return jsonLdPrice;
        }

        Optional<BigDecimal> selectorPrice = extractWithTrendyolSelectors(context);
        if (selectorPrice.isPresent()) {
            log.info("Price extracted with Trendyol selectors: {}", selectorPrice.get());
            return selectorPrice;
        }

        Optional<BigDecimal> metaPrice = extractFromMetaTags(context);
        if (metaPrice.isPresent()) {
            log.info("Price extracted from meta tags: {}", metaPrice.get());
            return metaPrice;
        }

        Optional<BigDecimal> fallbackPrice = trySelectorsExtraction(context);
        if (fallbackPrice.isPresent()) {
            log.info("Price extracted with fallback selectors: {}", fallbackPrice.get());
            return fallbackPrice;
        }

        log.warn("Could not extract price from Trendyol page: {}", context.url());
        return Optional.empty();
    }

    private Optional<BigDecimal> extractFromJsonLd(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());
            Elements jsonLdScripts = doc.select("script[type='application/ld+json']");

            for (Element script : jsonLdScripts) {
                Optional<BigDecimal> price = parseJsonLdContent(script.html());
                if (price.isPresent()) {
                    return price;
                }
            }
        } catch (Exception e) {
            log.debug("JSON-LD extraction failed for Trendyol: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> parseJsonLdContent(String jsonContent) {
        try {
            if (jsonContent.contains("\"@type\":\"Product\"") && jsonContent.contains("\"offers\"")) {
                String[] lines = jsonContent.split("\n");
                for (String line : lines) {
                    if (line.contains("\"price\"") && line.contains(":")) {
                        String priceStr = line.split(":")[1]
                                .replaceAll("[\"\\s,}]", "")
                                .trim();
                        Optional<BigDecimal> price = parsePriceString(priceStr);
                        if (price.isPresent()) {
                            return price;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse JSON-LD content: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> extractWithTrendyolSelectors(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            String[] trendyolSelectors = {
                    ".prc-dsc",
                    ".product-price-container .prc-dsc",
                    "[data-test-id='price-current-price']",
                    ".product-detail-price .prc-dsc",
                    ".pr-in-w .prc-dsc",
                    ".prc-org"
            };

            for (String selector : trendyolSelectors) {
                Element priceElement = doc.selectFirst(selector);
                if (priceElement != null) {
                    String priceText = priceElement.text();
                    log.debug("Found price element with selector '{}': {}", selector, priceText);

                    Optional<BigDecimal> price = parseTrendyolPrice(priceText);
                    if (price.isPresent()) {
                        return price;
                    }
                }
            }

            Optional<BigDecimal> scriptPrice = extractFromScriptTags(doc);
            if (scriptPrice.isPresent()) {
                return scriptPrice;
            }

        } catch (Exception e) {
            log.debug("Trendyol selector extraction failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> parseTrendyolPrice(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return Optional.empty();
        }

        String cleaned = priceText
                .replaceAll("TL", "")
                .replaceAll("₺", "")
                .replaceAll("\\s+", "")
                .trim();

        String[] trendyolPatterns = {
                "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                "([0-9]+,[0-9]{2})",
                "([0-9]+\\.[0-9]{3},[0-9]{2})",
                "([0-9]+)"
        };

        for (String pattern : trendyolPatterns) {
            java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
            java.util.regex.Matcher matcher = regex.matcher(cleaned);

            if (matcher.find()) {
                try {
                    String priceMatch = matcher.group(1);
                    String normalized = priceMatch
                            .replace(".", "")
                            .replace(",", ".");

                    BigDecimal price = new BigDecimal(normalized);
                    log.debug("Parsed Trendyol price: {} -> {}", priceText, price);
                    return Optional.of(price);
                } catch (NumberFormatException e) {
                    log.debug("Failed to parse price match '{}': {}", matcher.group(1), e.getMessage());
                }
            }
        }

        return Optional.empty();
    }

    private Optional<BigDecimal> extractFromMetaTags(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            String[] metaSelectors = {
                    "meta[property='product:price:amount']",
                    "meta[property='og:price:amount']",
                    "meta[name='price']",
                    "meta[property='price']"
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
            log.debug("Meta tag extraction failed for Trendyol: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> extractFromScriptTags(Document doc) {
        try {
            Elements scripts = doc.select("script:not([src])");

            for (Element script : scripts) {
                String scriptContent = script.html();

                String[] jsPatterns = {
                        "\"price\"\\s*:\\s*([0-9]+(?:\\.[0-9]{2})?)",
                        "\"currentPrice\"\\s*:\\s*([0-9]+(?:\\.[0-9]{2})?)",
                        "price:\\s*([0-9]+(?:\\.[0-9]{2})?)",
                        "currentPrice:\\s*([0-9]+(?:\\.[0-9]{2})?)"
                };

                for (String pattern : jsPatterns) {
                    java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
                    java.util.regex.Matcher matcher = regex.matcher(scriptContent);

                    if (matcher.find()) {
                        try {
                            String priceStr = matcher.group(1);
                            BigDecimal price = new BigDecimal(priceStr);
                            log.debug("Price extracted from script with pattern '{}': {}", pattern, price);
                            return Optional.of(price);
                        } catch (NumberFormatException e) {
                            log.debug("Failed to parse script price '{}': {}", matcher.group(1), e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Script tag extraction failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    @Override
    public boolean isValidUrl(String url) {
        if (!super.isValidUrl(url)) {
            return false;
        }

        return url.contains("/p/") || url.contains("/product/") || url.matches(".*trendyol\\.com.*\\d+.*");
    }
}