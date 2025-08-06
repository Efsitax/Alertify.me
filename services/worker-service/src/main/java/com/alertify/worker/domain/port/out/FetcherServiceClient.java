package com.alertify.worker.domain.port.out;

import com.alertify.worker.domain.entity.Monitor;
import com.alertify.worker.domain.model.MetricSample;

public interface FetcherServiceClient {

    MetricSample fetchMetric(Monitor monitor);

    default boolean supports(String sourceType) {
        return true;
    }
}
