package com.alertify.fetcher.domain.model;

import java.util.Map;

public record ExtractionContext(
        String url,
        String html,
        Map<String, String> params
) {
}
