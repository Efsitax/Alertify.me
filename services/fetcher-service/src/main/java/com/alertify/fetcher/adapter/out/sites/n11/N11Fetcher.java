package com.alertify.fetcher.adapter.out.sites.n11;

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class N11Fetcher extends AbstractSiteFetcher {

    private static final List<String> SUPPORTED_DOMAINS = List.of(
            "n11.com",
            "www.n11.com"
    );

    private static final SiteConfig N11_CONFIG = SiteConfig.builder()
            .priceSelectors(List.of(
                    ".newPrice",
                    ".newPrice ins",
                    ".priceContainer .newPrice",
                    ".urunPriceClass",
                    ".currentPrice",
                    "[data-price]",
                    ".price",
                    ".productPrice",
                    ".salePrice"
            ))
            .fallbackSelectors(List.of(
                    ".price-container",
                    ".product-price",
                    ".sale-price",
                    ".current-price",
                    "[class*='price']",
                    "[class*='fiyat']"
            ))
            .priceRegexPatterns(List.of(
                    "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                    "([0-9]{1,3}(?:\\.[0-9]{3})+)(?=\\s*TL)",
                    "([0-9]+,[0-9]{2})",
                    "([0-9]+)(?=\\s*TL)",
                    "([0-9]{1,3}(?:\\,[0-9]{3})*\\.[0-9]{2})"
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
                    "Sec-Fetch-User", "?1"
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

    public N11Fetcher(SeleniumWebDriverService seleniumService) {
        super(seleniumService);
    }

    @Override
    public boolean supportsDomain(String domain) {
        return SUPPORTED_DOMAINS.stream()
                .anyMatch(supportedDomain -> domain.toLowerCase().contains(supportedDomain));
    }

    @Override
    public String getSiteName() {
        return "N11";
    }

    @Override
    public List<String> getSupportedDomains() {
        return SUPPORTED_DOMAINS;
    }

    @Override
    public SiteConfig getConfiguration() {
        return N11_CONFIG;
    }

    @Override
    public int getPriority() {
        return 15;
    }

    @Override
    protected Optional<BigDecimal> extractPrice(ExtractionContext context) {
        log.info("Extracting price from N11 using specialized logic");

        Optional<BigDecimal> selectorPrice = extractWithN11Selectors(context);
        if (selectorPrice.isPresent()) {
            log.info("Price extracted with N11 selectors: {}", selectorPrice.get());
            return selectorPrice;
        }

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

        Optional<BigDecimal> textPrice = extractFromPageText(context);
        if (textPrice.isPresent()) {
            log.info("Price extracted from page text: {}", textPrice.get());
            return textPrice;
        }

        Optional<BigDecimal> fallbackPrice = trySelectorsExtraction(context);
        if (fallbackPrice.isPresent()) {
            log.info("Price extracted with fallback selectors: {}", fallbackPrice.get());
            return fallbackPrice;
        }

        log.warn("Could not extract price from N11 page: {}", context.url());
        return Optional.empty();
    }

    private Optional<BigDecimal> extractWithN11Selectors(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            String[] n11Selectors = {
                    ".newPrice",
                    ".newPrice ins",
                    ".priceContainer .newPrice",
                    ".urunPriceClass",
                    ".currentPrice",
                    ".productPrice",
                    ".salePrice",
                    ".price",
                    "[data-price]"
            };

            for (String selector : n11Selectors) {
                Elements elements = doc.select(selector);
                for (Element element : elements) {

                    if (selector.equals(".newPrice")) {
                        Optional<BigDecimal> insPrice = extractFromN11NewPriceDiv(element);
                        if (insPrice.isPresent()) {
                            log.debug("N11 .newPrice div extraction successful: {}", insPrice.get());
                            return insPrice;
                        }
                    }

                    String priceText = element.text().trim();
                    if (!priceText.isEmpty()) {
                        Optional<BigDecimal> price = parseN11Price(priceText);
                        if (price.isPresent()) {
                            log.debug("N11 selector '{}' found price: {}", selector, price.get());
                            return price;
                        }
                    }

                    String dataPrice = element.attr("data-price");
                    if (!dataPrice.isEmpty()) {
                        Optional<BigDecimal> price = parseN11Price(dataPrice);
                        if (price.isPresent()) {
                            log.debug("N11 data-price attribute found: {}", price.get());
                            return price;
                        }
                    }
                }
            }

        } catch (Exception e) {
            log.debug("N11 selector extraction failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> extractFromN11NewPriceDiv(Element newPriceDiv) {
        try {
            Element insElement = newPriceDiv.selectFirst("ins");
            if (insElement != null) {
                String contentAttr = insElement.attr("content");
                if (!contentAttr.isEmpty()) {
                    try {
                        BigDecimal price = new BigDecimal(contentAttr);
                        log.debug("N11 ins[content] found: {}", price);
                        return Optional.of(price);
                    } catch (NumberFormatException e) {
                        log.debug("Failed to parse ins content: {}", contentAttr);
                    }
                }

                String insText = insElement.ownText().trim();
                if (!insText.isEmpty()) {
                    Optional<BigDecimal> price = parseN11Price(insText);
                    if (price.isPresent()) {
                        log.debug("N11 ins text found: {}", price.get());
                        return price;
                    }
                }
            }

            String divText = newPriceDiv.ownText().trim();
            if (!divText.isEmpty()) {
                Optional<BigDecimal> price = parseN11Price(divText);
                if (price.isPresent()) {
                    log.debug("N11 .newPrice div text found: {}", price.get());
                    return price;
                }
            }

        } catch (Exception e) {
            log.debug("N11 .newPrice div processing failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> parseN11Price(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            return Optional.empty();
        }

        String cleaned = priceText
                .replaceAll("TL", "")
                .replaceAll("₺", "")
                .replaceAll("\\s+", "")
                .replaceAll("[^0-9.,]", "")
                .trim();

        log.debug("N11 price parsing: '{}' -> '{}'", priceText, cleaned);

        String[] n11Patterns = {
                "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                "([0-9]{1,3}(?:\\.[0-9]{3})+)(?![0-9])",
                "([0-9]+,[0-9]{2})",
                "([0-9]+)(?![0-9.,])",
                "([0-9]{1,3}(?:\\,[0-9]{3})*\\.[0-9]{2})"
        };

        for (String pattern : n11Patterns) {
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(cleaned);

            if (matcher.find()) {
                try {
                    String priceMatch = matcher.group(1);
                    String normalized = normalizeN11Price(priceMatch);
                    BigDecimal price = new BigDecimal(normalized);

                    log.debug("N11 price parsed with pattern '{}': {} -> {}",
                            pattern, priceMatch, price);
                    return Optional.of(price);

                } catch (NumberFormatException e) {
                    log.debug("Failed to parse N11 price match '{}': {}",
                            matcher.group(1), e.getMessage());
                }
            }
        }

        return Optional.empty();
    }

    private String normalizeN11Price(String priceStr) {
        if (priceStr == null || priceStr.isEmpty()) {
            return "0";
        }

        if (priceStr.contains(",") && priceStr.contains(".")) {
            int lastCommaIndex = priceStr.lastIndexOf(",");
            String beforeComma = priceStr.substring(0, lastCommaIndex).replace(".", "");
            String afterComma = priceStr.substring(lastCommaIndex + 1);
            return beforeComma + "." + afterComma;
        }
        else if (priceStr.contains(".") && !priceStr.contains(",")) {
            String[] parts = priceStr.split("\\.");
            if (parts.length > 1 && parts[parts.length - 1].length() == 3) {
                return priceStr.replace(".", "");
            } else {
                return priceStr;
            }
        }
        else if (priceStr.contains(",")) {
            return priceStr.replace(",", ".");
        }
        else {
            return priceStr;
        }
    }

    private Optional<BigDecimal> extractFromJsonLd(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());
            Elements jsonLdScripts = doc.select("script[type='application/ld+json']");

            for (Element script : jsonLdScripts) {
                String jsonContent = script.html();
                Optional<BigDecimal> price = parseJsonLdForPrice(jsonContent);
                if (price.isPresent()) {
                    return price;
                }
            }
        } catch (Exception e) {
            log.debug("JSON-LD extraction failed for N11: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> parseJsonLdForPrice(String jsonContent) {
        try {
            if (jsonContent.contains("\"@type\":\"Product\"") ||
                    jsonContent.contains("\"@type\":\"Offer\"")) {

                String[] pricePatterns = {
                        "\"price\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]{2})?)\"?",
                        "\"priceValue\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]{2})?)\"?",
                        "\"amount\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]{2})?)\"?",
                        "\"lowPrice\"\\s*:\\s*\"?([0-9]+(?:\\.[0-9]{2})?)\"?"
                };

                for (String pattern : pricePatterns) {
                    Pattern regex = Pattern.compile(pattern);
                    Matcher matcher = regex.matcher(jsonContent);

                    if (matcher.find()) {
                        try {
                            String priceStr = matcher.group(1);
                            BigDecimal price = new BigDecimal(priceStr);
                            log.debug("N11 JSON-LD price found: {}", price);
                            return Optional.of(price);
                        } catch (NumberFormatException e) {
                            log.debug("Failed to parse N11 JSON-LD price: {}", matcher.group(1));
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("Failed to parse N11 JSON-LD content: {}", e.getMessage());
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
                    "meta[property='price']",
                    "meta[name='product:price']"
            };

            for (String selector : metaSelectors) {
                Element metaElement = doc.selectFirst(selector);
                if (metaElement != null) {
                    String content = metaElement.attr("content");
                    if (!content.isEmpty()) {
                        Optional<BigDecimal> price = parseN11Price(content);
                        if (price.isPresent()) {
                            log.debug("N11 meta tag '{}' found price: {}", selector, price.get());
                            return price;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.debug("N11 meta tag extraction failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private Optional<BigDecimal> extractFromPageText(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());
            doc.select("script, style, noscript").remove();
            String pageText = doc.text();

            String[] textPatterns = {
                    "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})\\s*TL",
                    "([0-9]+,[0-9]{2})\\s*TL",
                    "₺\\s*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                    "₺\\s*([0-9]+,[0-9]{2})",
                    "Fiyat[:\\s]*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})"
            };

            for (String pattern : textPatterns) {
                Pattern regex = Pattern.compile(pattern);
                Matcher matcher = regex.matcher(pageText);

                if (matcher.find()) {
                    String priceStr = matcher.group(1);
                    Optional<BigDecimal> price = parseN11Price(priceStr);
                    if (price.isPresent() && isReasonablePrice(price.get())) {
                        log.debug("N11 text pattern '{}' found price: {}", pattern, price.get());
                        return price;
                    }
                }
            }
        } catch (Exception e) {
            log.debug("N11 page text extraction failed: {}", e.getMessage());
        }
        return Optional.empty();
    }

    private boolean isReasonablePrice(BigDecimal price) {
        return price.compareTo(BigDecimal.valueOf(1)) >= 0 &&
                price.compareTo(BigDecimal.valueOf(100000)) <= 0;
    }

    @Override
    public boolean isValidUrl(String url) {
        if (!super.isValidUrl(url)) {
            return false;
        }

        return url.contains("n11.com") &&
                (url.contains("/urun/") || url.matches(".*n11\\.com.*\\d+.*"));
    }
}