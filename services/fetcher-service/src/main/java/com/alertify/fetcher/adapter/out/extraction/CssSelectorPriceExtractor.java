package com.alertify.fetcher.adapter.out.extraction;

import com.alertify.fetcher.domain.model.ExtractionContext;
import com.alertify.fetcher.domain.port.PriceExtractionStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Component
public class CssSelectorPriceExtractor implements PriceExtractionStrategy {

    private final Map<String, String[]> domainSelectors;

    public CssSelectorPriceExtractor() {
        this.domainSelectors = new HashMap<>();
        initializeDomainSelectors();
    }

    private void initializeDomainSelectors() {
        domainSelectors.put("trendyol.com", new String[] {
                ".prc-dsc",
                ".prc-org",
                ".product-price-container .prc-dsc"
        });

        domainSelectors.put("hepsiburada.com", new String[] {
                "#offering-price",
                ".price-value",
                ".product-price .price-value"
        });

        domainSelectors.put("n11.com", new String[]{
                ".newPrice",
                ".priceContainer .newPrice"
        });

        domainSelectors.put("*", new String[]{
                "[data-price]",
                ".price",
                ".product-price",
                ".current-price",
                ".sale-price"
        });
    }

    @Override
    public Optional<BigDecimal> extractPrice(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());
            String domain = extractDomain(context.url());

            String[] selectors = domainSelectors.get(domain);
            if (selectors != null) {
                Optional<BigDecimal> price = trySelectors(doc, selectors);
                if (price.isPresent()) {
                    log.info("Price extracted from domain selector for {}: {}", domain, price.get());
                    return price;
                }
            }

            String[] genericSelectors = domainSelectors.get("*");
            Optional<BigDecimal> price = trySelectors(doc, genericSelectors);
            if (price.isPresent()) {
                log.info("Price extracted with generic selector: {}", price.get());
                return price;
            }

            log.debug("No price found with CSS selectors for URL: {}", context.url());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Error with CSS selector extraction for URL {}: {}", context.url(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> trySelectors(Document doc, String[] selectors) {
        for (String selector : selectors) {
            Element priceElement = doc.selectFirst(selector);
            if (priceElement != null) {
                String priceText = priceElement.text();
                Optional<BigDecimal> price = parsePriceString(priceText);
                if (price.isPresent()) {
                    return price;
                }

                String dataPrice = priceElement.attr("data-price");
                if (!dataPrice.isEmpty()) {
                    Optional<BigDecimal> dataPriceValue = parsePriceString(dataPrice);
                    if (dataPriceValue.isPresent()) {
                        return dataPriceValue;
                    }
                }
            }
        }
        return Optional.empty();
    }

    private String extractDomain(String url) {
        try {
            URI uri = new URI(url);
            String host = uri.getHost();
            return host != null ? host.toLowerCase() : "";
        } catch (Exception e) {
            return "";
        }
    }

    private Optional<BigDecimal> parsePriceString(String priceText) {
        try {
            log.debug("Attempting to parse price text: {}", priceText);

            String[] patterns = {
                    "([0-9]{1,3}(?:\\.[0-9]{3})*,[0-9]{2})",
                    "([0-9]{1,3}(?:\\,[0-9]{3})*\\.[0-9]{2})",
                    "([0-9]+,[0-9]{2})",
                    "([0-9]+\\.[0-9]{2})",
                    "([0-9]+)"
            };

            for (String pattern : patterns) {
                java.util.regex.Pattern regex = java.util.regex.Pattern.compile(pattern);
                java.util.regex.Matcher matcher = regex.matcher(priceText);

                if (matcher.find()) {
                    String priceMatch = matcher.group(1);
                    log.debug("Found price match with pattern {}: {}", pattern, priceMatch);

                    String normalized = priceMatch
                            .replace(".", "")
                            .replace(",", ".");

                    BigDecimal price = new BigDecimal(normalized);
                    log.info("Successfully parsed price: {} from text: {}", price, priceText);
                    return Optional.of(price);
                }
            }

            log.debug("No price pattern matched for text: {}", priceText);
            return Optional.empty();

        } catch (Exception e) {
            log.debug("Could not parse price string: {} - Error: {}", priceText, e.getMessage());
            return Optional.empty();
        }
    }

    @Override
    public int getPriority() {
        return 3;
    }
}
