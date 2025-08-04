package com.alertify.worker.adapter.out.http;

import com.alertify.worker.domain.entity.Monitor;
import com.alertify.worker.domain.exception.ClientException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class MonitorServiceClient {

    private final WebClient webClient;

    public MonitorServiceClient(@Value("${services.monitor-service.url}") String monitorServiceUrl) {
        this.webClient = WebClient.builder()
                .baseUrl(monitorServiceUrl)
                .build();
    }

    public List<Monitor> fetchActiveMonitors() {
        try {
            Monitor[] monitors = webClient.get()
                    .uri("/api/monitors?status=ACTIVE")
                    .retrieve()
                    .bodyToMono(Monitor[].class)
                    .block();

            return monitors != null ? Arrays.asList(monitors) : List.of();
        } catch (Exception e) {
            log.error("Error fetching active monitors: {}", e.getMessage(), e);
            throw new ClientException("Error fetching active monitors", e);
        }
    }
}
