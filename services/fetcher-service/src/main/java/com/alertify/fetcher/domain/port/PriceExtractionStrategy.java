package com.alertify.fetcher.domain.port;

import com.alertify.fetcher.domain.model.ExtractionContext;

import java.math.BigDecimal;
import java.util.Optional;

public interface PriceExtractionStrategy {
    Optional<BigDecimal> extractPrice(ExtractionContext context);
    int getPriority();
}
