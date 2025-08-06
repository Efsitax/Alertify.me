package com.alertify.fetcher.domain.port;

import com.alertify.fetcher.domain.model.MetricSample;

import java.util.Map;

public interface MetricFetcher {
    boolean supports(String sourceType);
    MetricSample fetch(Map<String, String> params);
}
