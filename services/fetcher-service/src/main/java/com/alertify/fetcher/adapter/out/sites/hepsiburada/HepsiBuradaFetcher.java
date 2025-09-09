package com.alertify.fetcher.adapter.out.sites.hepsiburada;

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
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
public class HepsiBuradaFetcher extends AbstractSiteFetcher {

    private static final List<String> SUPPORTED_DOMAINS = List.of(
            "hepsiburada.com",
            "www.hepsiburada.com"
    );

    private static final SiteConfig HEPSIBURADA_CONFIG = SiteConfig.builder()
            .userAgent("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            .timeoutMs(20000)
            .requiresJs(true)
            .useSelenium(true)
            .waitAfterLoadMs(5000)
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
        return "HepsiBurada-XPath";
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
        return 10;
    }

    @Override
    protected Optional<BigDecimal> extractPrice(ExtractionContext context) {
        log.info("Extracting price from HepsiBurada using ANTI-OBFUSCATION techniques");

        Optional<BigDecimal> xpathPrice = extractWithXPathTextBased(context);
        if (xpathPrice.isPresent()) {
            log.info("SUCCESS: XPath text-based extraction: {}", xpathPrice.get());
            return xpathPrice;
        }

        Optional<BigDecimal> stylePrice = extractByComputedStyle(context);
        if (stylePrice.isPresent()) {
            log.info("SUCCESS: Computed style extraction: {}", stylePrice.get());
            return stylePrice;
        }

        Optional<BigDecimal> structuralPrice = extractByStructuralPattern(context);
        if (structuralPrice.isPresent()) {
            log.info("SUCCESS: Structural pattern extraction: {}", structuralPrice.get());
            return structuralPrice;
        }

        Optional<BigDecimal> proximityPrice = extractByTextProximity(context);
        if (proximityPrice.isPresent()) {
            log.info("SUCCESS: Text proximity extraction: {}", proximityPrice.get());
            return proximityPrice;
        }

        log.warn("ALL ANTI-OBFUSCATION STRATEGIES FAILED for URL: {}", context.url());
        return Optional.empty();
    }

    private Optional<BigDecimal> extractWithXPathTextBased(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            String[] textPatterns = {
                    "sepete özel",
                    "özel fiyat",
                    "indirimli",
                    "kazancınız",
                    "tasarruf"
            };

            for (String pattern : textPatterns) {
                Optional<BigDecimal> price = findPriceByTextContext(doc, pattern);
                if (price.isPresent()) {
                    log.debug("XPath text pattern '{}' found price: {}", pattern, price.get());
                    return price;
                }
            }

            Optional<BigDecimal> currencyPrice = findPriceByCurrencyProximity(doc);
            if (currencyPrice.isPresent()) {
                log.debug("Currency proximity found price: {}", currencyPrice.get());
                return currencyPrice;
            }

            return Optional.empty();

        } catch (Exception e) {
            log.debug("XPath text-based extraction failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> findPriceByTextContext(Document doc, String contextText) {
        Elements contextElements = doc.getElementsContainingOwnText(contextText);

        for (Element contextElement : contextElements) {
            Set<Element> relatedElements = new HashSet<>();

            Element parent = contextElement.parent();
            if (parent != null) {
                relatedElements.addAll(parent.getAllElements());
            }

            Element grandParent = parent != null ? parent.parent() : null;
            if (grandParent != null) {
                relatedElements.addAll(grandParent.getAllElements());
            }

            Elements followingSiblings = contextElement.nextElementSiblings();
            relatedElements.addAll(followingSiblings);

            for (Element element : relatedElements) {
                String text = element.ownText().trim();
                if (containsNumericPrice(text)) {
                    Optional<BigDecimal> price = parseHepsiBuradaPrice(text);
                    if (price.isPresent() && isReasonablePrice(price.get())) {
                        log.debug("Found price {} near context '{}'", price.get(), contextText);
                        return price;
                    }
                }
            }
        }

        return Optional.empty();
    }

    private Optional<BigDecimal> findPriceByCurrencyProximity(Document doc) {
        String[] currencySymbols = {"₺", "TL", "tl"};

        for (String currency : currencySymbols) {
            Elements currencyElements = doc.getElementsContainingOwnText(currency);

            for (Element currencyElement : currencyElements) {
                String fullText = currencyElement.text();

                String[] patterns = {
                        "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})\\s*(?:" + currency + ")",
                        "([0-9]+,[0-9]{2})\\s*(?:" + currency + ")",
                        "(?:" + currency + ")\\s*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                        "(?:" + currency + ")\\s*([0-9]+,[0-9]{2})"
                };

                for (String pattern : patterns) {
                    Pattern regex = Pattern.compile(pattern);
                    Matcher matcher = regex.matcher(fullText);
                    if (matcher.find()) {
                        try {
                            String priceStr = matcher.group(1);
                            Optional<BigDecimal> price = parseHepsiBuradaPrice(priceStr);
                            if (price.isPresent() && isReasonablePrice(price.get())) {
                                return price;
                            }
                        } catch (Exception e) {
                            // continue
                        }
                    }
                }
            }
        }

        return Optional.empty();
    }

    private Optional<BigDecimal> extractByComputedStyle(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            Map<BigDecimal, Integer> priceStyleScores = new HashMap<>();

            Elements allElements = doc.select("*");
            for (Element element : allElements) {
                String text = element.ownText().trim();
                if (containsNumericPrice(text)) {
                    Optional<BigDecimal> price = parseHepsiBuradaPrice(text);
                    if (price.isPresent() && isReasonablePrice(price.get())) {
                        int styleScore = calculateStyleScore(element);
                        priceStyleScores.put(price.get(), styleScore);
                        log.debug("Style-based candidate: {} (score: {})", price.get(), styleScore);
                    }
                }
            }

            return priceStyleScores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);

        } catch (Exception e) {
            log.debug("Computed style extraction failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private int calculateStyleScore(Element element) {
        int score = 0;
        String tagName = element.tagName().toLowerCase();
        String className = element.className().toLowerCase();

        switch (tagName) {
            case "h1" -> score += 15;
            case "h2" -> score += 12;
            case "h3" -> score += 10;
            case "strong", "b" -> score += 8;
            case "span" -> score += 5;
            case "div" -> score += 3;
        }

        if (className.contains("price") || className.contains("cost") ||
                className.contains("amount") || className.contains("value")) {
            score += 10;
        }

        if (className.contains("special") || className.contains("discount") ||
                className.contains("sale") || className.contains("offer")) {
            score += 8;
        }

        if (className.contains("current") || className.contains("main") ||
                className.contains("primary")) {
            score += 6;
        }

        Element parent = element.parent();
        if (parent != null) {
            String parentText = parent.text().toLowerCase();
            if (parentText.contains("sepete özel") || parentText.contains("özel fiyat")) {
                score += 20;
            }
            if (parentText.contains("indirimli") || parentText.contains("kampanya")) {
                score += 15;
            }
            if (parentText.contains("kazancınız") || parentText.contains("tasarruf")) {
                score += 10;
            }
        }

        return score;
    }

    private Optional<BigDecimal> extractByStructuralPattern(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            String[] structuralSelectors = {
                    "[class*='price']:not([class*='old']):not([class*='original'])",
                    "[class*='amount']:not([class*='was'])",
                    "[class*='cost']:not([class*='prev'])",
                    "[class*='special']",
                    "[class*='discount']",
                    "[class*='offer']",

                    "[data-price]",
                    "[data-amount]",
                    "[data-cost]",
                    "[data-value]",

                    "main [role='main'] *",
                    ".container *",
                    "#content *",

                    "h1 + * *", "h2 + * *", "h3 + * *"
            };

            Map<BigDecimal, Integer> structuralScores = new HashMap<>();

            for (String selector : structuralSelectors) {
                try {
                    Elements elements = doc.select(selector);
                    for (Element element : elements) {
                        String text = element.ownText().trim();
                        if (containsNumericPrice(text)) {
                            Optional<BigDecimal> price = parseHepsiBuradaPrice(text);
                            if (price.isPresent() && isReasonablePrice(price.get())) {
                                int selectorScore = getSelectorScore(selector);
                                structuralScores.merge(price.get(), selectorScore, Integer::sum);
                                log.debug("Structural pattern '{}' found price: {} (score: {})",
                                        selector, price.get(), selectorScore);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.debug("Selector '{}' failed: {}", selector, e.getMessage());
                }
            }

            return structuralScores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);

        } catch (Exception e) {
            log.debug("Structural pattern extraction failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private int getSelectorScore(String selector) {
        int score = 0;

        if (selector.contains("special") || selector.contains("discount") || selector.contains("offer")) {
            score += 15;
        }
        if (selector.contains("price") || selector.contains("amount") || selector.contains("cost")) {
            score += 12;
        }
        if (selector.contains("data-")) {
            score += 10; // Data attributes are usually stable
        }
        if (selector.contains("main") || selector.contains("content")) {
            score += 8;
        }
        if (selector.contains("h1") || selector.contains("h2")) {
            score += 6;
        }

        return score;
    }

    private Optional<BigDecimal> extractByTextProximity(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());

            doc.select("script, style, noscript").remove();
            String cleanText = doc.text();

            String[] contextualPatterns = {
                    "sepete\\s+özel[^0-9]*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})\\s*TL",
                    "özel\\s+fiyat[^0-9]*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})\\s*TL",

                    "₺\\s*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                    "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})\\s*₺",

                    "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})\\s*TL",
                    "([0-9]+,[0-9]{2})\\s*TL",

                    "fiyat[ı:]?\\s*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                    "tutar[ı:]?\\s*([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",

                    "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})(?=\\s*$)",
                    "([0-9]+,[0-9]{2})(?=\\s*TL\\s*$)"
            };

            Map<BigDecimal, Integer> proximityScores = new HashMap<>();

            for (int i = 0; i < contextualPatterns.length; i++) {
                String pattern = contextualPatterns[i];
                Pattern regex = Pattern.compile(pattern, Pattern.CASE_INSENSITIVE);
                Matcher matcher = regex.matcher(cleanText);

                while (matcher.find()) {
                    try {
                        String priceStr = matcher.group(1);
                        BigDecimal price = parseHepsiBuradaPriceStrict(priceStr);

                        if (isReasonablePrice(price)) {
                            int patternScore = contextualPatterns.length - i;
                            proximityScores.merge(price, patternScore, Integer::sum);

                            log.debug("Text proximity pattern '{}' found price: {} (score: {})",
                                    pattern, price, patternScore);
                        }
                    } catch (Exception e) {
                        // continue
                    }
                }
            }

            return proximityScores.entrySet().stream()
                    .max(Map.Entry.comparingByValue())
                    .map(Map.Entry::getKey);

        } catch (Exception e) {
            log.debug("Text proximity analysis failed: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private boolean containsNumericPrice(String text) {
        if (text == null || text.trim().isEmpty()) {
            return false;
        }

        return text.matches(".*[0-9]{1,3}(?:[\\.,][0-9]{3})*[\\.,]?[0-9]{0,2}.*") &&
                (text.contains("TL") || text.contains("₺") ||
                        text.matches(".*[0-9]+[\\.,][0-9]{2}.*"));
    }

    private Optional<BigDecimal> parseHepsiBuradaPrice(String priceText) {
        try {
            BigDecimal price = parseHepsiBuradaPriceStrict(priceText);
            return Optional.of(price);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    private BigDecimal parseHepsiBuradaPriceStrict(String priceText) {
        if (priceText == null || priceText.trim().isEmpty()) {
            throw new NumberFormatException("Empty price text");
        }

        String cleaned = priceText
                .replaceAll("TL", "")
                .replaceAll("₺", "")
                .replaceAll("\\s+", "")
                .replaceAll("[^0-9.,]", "")
                .trim();

        String[] patterns = {
                "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                "([0-9]+,[0-9]{2})",
                "([0-9]{1,3}(?:\\,[0-9]{3})*\\.[0-9]{2})",
                "([0-9]+\\.[0-9]{2})",
                "([0-9]+)"
        };

        for (String pattern : patterns) {
            Pattern regex = Pattern.compile(pattern);
            Matcher matcher = regex.matcher(cleaned);

            if (matcher.find()) {
                String priceMatch = matcher.group(1);
                String normalized;

                if (priceMatch.contains(",") && priceMatch.contains(".")) {
                    int lastComma = priceMatch.lastIndexOf(",");
                    int lastDot = priceMatch.lastIndexOf(".");

                    if (lastComma > lastDot) {
                        normalized = priceMatch.substring(0, lastComma).replace(".", "") +
                                "." + priceMatch.substring(lastComma + 1);
                    } else {
                        normalized = priceMatch.substring(0, lastDot).replace(",", "") +
                                "." + priceMatch.substring(lastDot + 1);
                    }
                } else if (priceMatch.contains(",")) {
                    if (priceMatch.matches(".*,[0-9]{2}$")) {
                        normalized = priceMatch.replace(",", ".");
                    } else {
                        normalized = priceMatch.replace(",", "");
                    }
                } else {
                    normalized = priceMatch;
                }

                return new BigDecimal(normalized);
            }
        }

        throw new NumberFormatException("Could not parse price: " + priceText);
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

        return url.contains("hepsiburada.com") &&
                (url.contains("-p-") || url.contains("/product/") || url.matches(".*\\d+.*"));
    }
}