package com.alertify.monitor.adapter.out.jpa.adapter;

import com.alertify.monitor.BaseIntegrationTest;
import com.alertify.monitor.domain.entity.Monitor;
import com.alertify.monitor.domain.entity.Rule;
import com.alertify.monitor.domain.repository.MonitorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MonitorRepositoryAdapterTest extends BaseIntegrationTest {

    @Autowired
    private MonitorRepository monitorRepository;

    @Test
    void saveAndFindMonitor() {
        Monitor monitor = Monitor.builder()
                .id(UUID.randomUUID())
                .tenantId(UUID.randomUUID())
                .sourceType("ECOMMERCE_PRODUCT")
                .url("https://dummy.com/product/1")
                .params(Map.of("store", "DummyStore", "currency", "TRY"))
                .notifyPolicy(Map.of("channels", List.of("EMAIL"), "throttleMinutes", "60"))
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        monitorRepository.save(monitor);

        List<Monitor> all = monitorRepository.findAll();
        assertThat(all).hasSize(1);
        assertThat(all.getFirst().getUrl()).isEqualTo("https://dummy.com/product/1");
    }

    @Test
    void findByStatusLoadsRules() {
        Monitor monitor = Monitor.builder()
                .tenantId(UUID.randomUUID())
                .sourceType("ECOMMERCE_PRODUCT")
                .url("https://dummy.com/product/2")
                .params(Map.of("store", "DummyStore", "currency", "TRY"))
                .notifyPolicy(Map.of("channels", List.of("EMAIL"), "throttleMinutes", "60"))
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        Rule rule = Rule.builder()
                .type("TARGET_PRICE")
                .config(Map.of("targetPrice", 1200))
                .build();

        monitor.setRules(List.of(rule));

        monitorRepository.save(monitor);

        List<Monitor> activeMonitors = monitorRepository.findByStatus("ACTIVE");

        assertThat(activeMonitors).hasSize(1);
        assertThat(activeMonitors.getFirst().getRules()).hasSize(1);
        assertThat(activeMonitors.getFirst().getRules().getFirst().getType())
                .isEqualTo("TARGET_PRICE");
    }
}