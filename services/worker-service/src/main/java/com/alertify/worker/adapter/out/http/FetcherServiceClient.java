package com.alertify.worker.adapter.out.http;

import com.alertify.worker.domain.entity.Monitor;
import com.alertify.worker.domain.model.MetricSample;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Random;

@Component
public class FetcherServiceClient {

    private final Random random = new Random();
    //private final WebClient webClient;

    /*public FetcherServiceClient(@Value("${services.fetcher-service.url}") String fetcherServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(fetcherServiceUrl)
                .build();
    }

    public MetricSample fetchMetric(Monitor monitor) {
        return webClient.post()
                .uri("/fetch")
                .bodyValue(monitor.getParams())
                .retrieve()
                .bodyToMono(MetricSample.class)
                .block();
    }*/

    public MetricSample fetchMetric(Monitor monitor) {

        BigDecimal value = BigDecimal.valueOf(1000 + (1000 * random.nextDouble()));
        return new MetricSample(
                "price",
                value,
                "TRY",
                Instant.now()
        );
    }


}