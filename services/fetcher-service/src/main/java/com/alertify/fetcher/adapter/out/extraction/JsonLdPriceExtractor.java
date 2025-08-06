package com.alertify.fetcher.adapter.out.extraction;

import com.alertify.fetcher.domain.model.ExtractionContext;
import com.alertify.fetcher.domain.port.PriceExtractionStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class JsonLdPriceExtractor implements PriceExtractionStrategy {

    private final ObjectMapper objectMapper;

    @Override
    public Optional<BigDecimal> extractPrice(ExtractionContext context) {
        try {
            Document doc = Jsoup.parse(context.html());
            Elements jsonLdScripts = doc.select("script[type='application/ld+json']");

            for (Element script : jsonLdScripts) {
                Optional<BigDecimal> price = parseJsonLdScript(script.html());
                if (price.isPresent()) {
                    log.info("Price extracted from JSON-LD: {}", price.get());
                    return price;
                }
            }

            log.debug("No price found in JSON-LD for URL: {}", context.url());
            return Optional.empty();
        } catch (Exception e) {
            log.warn("Error parsing JSON-LD for URL {}: {}", context.url(), e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> parseJsonLdScript(String jsonContent) {
        try {
            JsonNode root = objectMapper.readTree(jsonContent);

            if (root.isArray() && !root.isEmpty()) {
                root = root.get(0);
            }

            if ("Product".equals(root.path("@type").asText())) {
                return extractPriceFromProduct(root);
            }

            return Optional.empty();
        } catch (Exception e) {
            log.debug("Failed to parse JSON-LD content: {}", e.getMessage());
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> extractPriceFromProduct(JsonNode productNode) {
        JsonNode offers = productNode.path("offers");

        if (offers.isArray() && !offers.isEmpty()) {
            offers = offers.get(0);
        }

        if (!offers.isMissingNode()) {
            String priceText = offers.path("price").asText();
            if (!priceText.isEmpty()) {
                return parePriceString(priceText);
            }
        }

        return Optional.empty();
    }

    private Optional<BigDecimal> parePriceString(String priceText) {
        try {
            String normalized = priceText
                    .replaceAll("[^0-9,.]", "")
                    .replace(",", ".");

            return Optional.of(new BigDecimal(normalized));
        } catch (NumberFormatException e) {
            log.debug("Could not parse price string: {}", priceText);
            return  Optional.empty();
        }
    }

    @Override
    public int getPriority() {
        return 1;
    }
}
