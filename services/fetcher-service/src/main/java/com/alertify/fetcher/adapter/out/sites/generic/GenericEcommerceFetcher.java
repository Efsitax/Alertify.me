package com.alertify.fetcher.adapter.out.sites.generic;

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
public class GenericEcommerceFetcher extends AbstractSiteFetcher {

    private static final List<String> SUPPORTED_DOMAINS = List.of("*");

    private static final SiteConfig GENERIC_CONFIG = SiteConfig.builder()
            .priceSelectors(List.of(
                    ".price",
                    ".product-price",
                    ".current-price",
                    ".sale-price",
                    ".regular-price",
                    ".final-price",
                    ".price-current",
                    ".price-now",
                    ".price-value",
                    "[data-price]",
                    "[data-testid*='price']",
                    "[data-test-id*='price']",
                    "[class*='price']:not([class*='old']):not([class*='was']):not([class*='original'])",
                    "[id*='price']:not([id*='old']):not([id*='was'])",
                    ".notranslate"
            ))
            .fallbackSelectors(List.of(
                    "[class*='cost']",
                    "[class*='amount']",
                    "[class*='value']",
                    "[id*='cost']",
                    "[id*='amount']",
                    ".money",
                    ".currency",
                    ".total"
            ))
            .priceRegexPatterns(List.of(
                    "([0-9]{1,3}(?:[.,][0-9]{3})*[.,][0-9]{2})",
                    "([0-9]+[.,][0-9]{2})",
                    "([0-9]{1,3}(?:[.,][0-9]{3})+)",
                    "([0-9]+)"
            ))
            .headers(Map.of(
                    "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
                    "Accept-Language", "en-US,en;q=0.5",
                    "Accept-Encoding", "gzip, deflate",
                    "Connection", "keep-alive",
                    "Upgrade-Insecure-Requests", "1"
            ))
            .userAgent("Mozilla/5.0 (compatible; AlertifyBot/1.0)")
            .timeoutMs(10000)
            .requiresJs(false)
            .useSelenium(false)
            .waitAfterLoadMs(1000)
            .defaultCurrency("USD")
            .enableJsonLd(true)
            .enableMetaTags(true)
            .build();

    public GenericEcommerceFetcher(SeleniumWebDriverService seleniumService) {
        super(seleniumService);
    }

    @Override
    public boolean supportsDomain(String domain) {
        return true; // Generic fetcher supports all domains
    }

    @Override
    public String getSiteName() {
        return "Generic E-commerce";
    }

    @Override
    public List<String> getSupportedDomains() {
        return SUPPORTED_DOMAINS;
    }

    @Override
    public SiteConfig getConfiguration() {
        return GENERIC_CONFIG;
    }

    @Override
    public int getPriority() {
        return 1000;
    }

    @Override
    protected Optional<BigDecimal> extractPrice(ExtractionContext context) {
        log.info("Extracting price using generic e-commerce strategies for: {}", context.url());

        Optional<BigDecimal> jsonLdPrice = extractFromJsonLd(context);
        if (jsonLdPrice.isPresent()) {
            log.info("Price extracted from JSON-LD: {}", jsonLdPrice.get());
            return jsonLdPrice;
        }

        Optional<BigDecimal> metaPrice = extractFromMetaTags(context);
        if (metaPrice.isPresent()) {
            log.info("Price extracted from meta tags: {}", metaPrice.get());
            return metaPrice;
        }

        Optional<BigDecimal> selectorPrice = trySelectorsExtraction(context);
        if (selectorPrice.isPresent()) {
            log.info("Price extracted with generic selectors: {}", selectorPrice.get());
            return selectorPrice;
        }

        Optional<BigDecimal> textPrice = extractFromTextContent(context);
        if (textPrice.isPresent()) {
            log.info("Price extracted from text content: {}", textPrice.get());
            return textPrice;
        }

        Optional<BigDecimal> microdataPrice = extractFromMicrodata(context);
        if (microdataPrice.isPresent()) {
            log.info("Price extracted from microdata: {}", microdataPrice.get());
            return microdataPrice;
        }

        log.warn("Could not extract price using generic strategies from: {}", context.url());
        return Optional.empty();
    }

    private Optional<BigDecimal> extractFromJsonLd(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());
            Elements jsonLdScripts = doc.select("script[type='application/ld+json']");

            for (Element script : jsonLdScripts) {
                String jsonContent = script.html();
                Optional<BigDecimal> price = parseGenericJsonLd(jsonContent);
                if (price.isPresent()) {
                    return price;
                }
            }
        } catch (Exception e) {
            log.debug("JSON-LD extraction failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> parseGenericJsonLd(String jsonContent) {
        try {
            if (jsonContent.contains("\"@type\"") &&
                    (jsonContent.contains("Product") || jsonContent.contains("Offer"))) {

                String[] priceFields = {
                        "\"price\"",
                        "\"priceValue\"",
                        "\"amount\"",
                        "\"value\"",
                        "\"cost\"",
                        "\"lowPrice\"",
                        "\"highPrice\""
                };

                for (String field : priceFields) {
                    String pattern = field + "\\s*:\\s*\"?([0-9]+(?:\\.[0-9]{2})?)\"?";
                    java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
                    java.util.regex.Matcher matcher = regex.matcher(jsonContent);

                    if (matcher.find()) {
                        try {
                            String priceStr = matcher.group(1);
                            return Optional.of(new BigDecimal(priceStr));
                        } catch (NumberFormatException e) {
                            log.debug("Failed to parse JSON-LD price from {}: {}", field, matcher.group(1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse generic JSON-LD: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> extractFromMetaTags(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            String[] metaSelectors = {
                    "meta[property='product:price:amount']",
                    "meta[property='og:price:amount']",
                    "meta[property='product:price']",
                    "meta[property='og:price']",
                    "meta[name='price']",
                    "meta[name='product:price']",
                    "meta[property='price']",
                    "meta[property='cost']",
                    "meta[name='twitter:data1']",
                    "meta[name='twitter:label1'][value*='price' i] + meta[name='twitter:data1']"
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
            log.debug("Meta tag extraction failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> extractFromTextContent(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            doc.select("script, style, noscript").remove();

            String pageText = doc.text();

            String[] pricePatterns = {
                    "\\$\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?)",
                    "€\\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?)",
                    "£\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?)",
                    "₹\\s*([0-9]{1,3}(?:,[0-9]{3})*(?:\\.[0-9]{2})?)",
                    "₺\\s*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?)",

                    "(?i)price[:\\s]*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?)",
                    "(?i)cost[:\\s]*([0-9]{1,3}(?:[.,][0-9]{3})*(?:[.,][0-9]{2})?)",
                    "(?i)\\$?([0-9]{1,3}(?:,[0-9]{3})*\\.[0-9]{2})\\s*(?:USD|usd)",

                    "([0-9]{1,3}(?:[.,][0-9]{3})*[.,][0-9]{2})(?=\\s*(?:TL|USD|EUR|GBP|₺|\\$|€|£))",
            };

            for (String pattern : pricePatterns) {
                java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher matcher = regex.matcher(pageText);

                if (matcher.find()) {
                    try {
                        String priceStr = matcher.group(1);
                        String normalized = normalizePriceString(priceStr);
                        BigDecimal price = new BigDecimal(normalized);

                        if (price.compareTo(BigDecimal.valueOf(0.01)) >= 0 &&
                                price.compareTo(BigDecimal.valueOf(1000000)) <= 0) {
                            log.debug("Price extracted from text with pattern '{}': {}", pattern, price);
                            return Optional.of(price);
                        }
                    } catch (NumberFormatException e) {
                        log.debug("Failed to parse text price '{}': {}", matcher.group(1), e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Text content extraction failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> extractFromMicrodata(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            String[] microdataSelectors = {
                    "[itemprop='price']",
                    "[itemprop='lowPrice']",
                    "[itemprop='highPrice']",
                    "[itemprop='amount']",
                    "[itemprop='value']",
                    "[itemtype*='Product'] [itemprop='offers'] [itemprop='price']",
                    "[itemtype*='Offer'] [itemprop='price']"
            };

            for (String selector : microdataSelectors) {
                Elements elements = doc.select(selector);
                for (Element element : elements) {
                    String content = element.attr("content");
                    if (!content.isEmpty()) {
                        Optional<BigDecimal> price = parsePriceString(content);
                        if (price.isPresent()) {
                            log.debug("Price extracted from microdata content '{}': {}", selector, price.get());
                            return price;
                        }
                    }

                    String text = element.text();
                    if (!text.isEmpty()) {
                        Optional<BigDecimal> price = parsePriceString(text);
                        if (price.isPresent()) {
                            log.debug("Price extracted from microdata text '{}': {}", selector, price.get());
                            return price;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Microdata extraction failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private String normalizePriceString(String priceStr) {
        if (priceStr == null) return "0";

        String cleaned = priceStr
                .replaceAll("[€£₹₺\\$]", "")
                .replaceAll("\\s+", "")
                .trim();

        if (cleaned.contains(",") && cleaned.contains(".")) {
            int lastComma = cleaned.lastIndexOf(",");
            int lastDot = cleaned.lastIndexOf(".");

            if (lastComma > lastDot) {
                cleaned = cleaned.substring(0, lastComma).replace(".", "").replace(",", "") +
                        "." + cleaned.substring(lastComma + 1);
            } else {
                cleaned = cleaned.substring(0, lastDot).replace(",", "").replace(".", "") +
                        "." + cleaned.substring(lastDot + 1);
            }
        } else if (cleaned.contains(",")) {
            if (cleaned.matches(".*,[0-9]{2}$")) {
                cleaned = cleaned.replace(",", ".");
            } else {
                cleaned = cleaned.replace(",", "");
            }
        }

        return cleaned;
    }

    @Override
    public boolean isValidUrl(String url) {
        if (url == null || url.trim().isEmpty()) {
            return false;
        }

        try {
            java.net.URI uri = new java.net.URI(url);
            return uri.getScheme() != null && uri.getHost() != null;
        } catch (Exception e) {
            return false;
        }
    }
}