package com.alertify.monitorservice.scheduler;

import com.alertify.monitorservice.BaseIntegrationTest;
import com.alertify.monitorservice.domain.entity.Alert;
import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.entity.Rule;
import com.alertify.monitorservice.domain.repository.AlertRepository;
import com.alertify.monitorservice.domain.repository.MonitorRepository;
import com.alertify.monitorservice.domain.repository.SnapshotRepository;
import com.alertify.monitorservice.scheduler.model.MetricSample;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

class MonitorSchedulerIntegrationTest extends BaseIntegrationTest {

    @Autowired private MonitorScheduler scheduler;
    @Autowired private MonitorRepository monitorRepository;
    @Autowired private SnapshotRepository snapshotRepository;
    @Autowired private AlertRepository alertRepository;

    @MockBean private MetricFetcherPort fetcher;

    private UUID monitorId;
    private UUID ruleId;

    private void prepareMonitor() {
        Monitor monitor = Monitor.builder()
                .tenantId(UUID.randomUUID())
                .sourceType("ECOMMERCE_PRODUCT")
                .url("https://dummy.com/product/scheduler")
                .params(Map.of("currency", "TRY", "store", "TestStore"))
                .notifyPolicy(Map.of("channels", List.of("EMAIL"), "throttleMinutes", 60))
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        Rule targetRule = Rule.builder()
                .type("TARGET_PRICE")
                .config(Map.of("targetPrice", 1000))
                .build();

        monitor.setRules(List.of(targetRule));
        Monitor saved = monitorRepository.save(monitor);

        this.monitorId = saved.getId();
        this.ruleId = saved.getRules().getFirst().getId();
    }

    @BeforeEach
    void resetMocks() {
        Mockito.reset(fetcher);
    }

    @Test
    void schedulerFetchesSampleAndTriggersAlert() {
        prepareMonitor();

        MetricSample sample = new MetricSample("price", BigDecimal.valueOf(900), "TRY", Instant.now());
        when(fetcher.fetch(Mockito.any())).thenReturn(sample);

        scheduler.runScheduledCheck();

        var snapshotOpt = snapshotRepository.findLastByMonitorId(monitorId);
        assertThat(snapshotOpt).isPresent();
        assertThat(snapshotOpt.get().getValue()).isEqualTo(BigDecimal.valueOf(900));

        Alert alert = alertRepository.save(Alert.builder()
                .monitorId(monitorId)
                .ruleId(ruleId)
                .message("Validation Alert")
                .firedAt(Instant.now())
                .build());

        assertThat(alert.getId()).isNotNull();
        assertThat(alert.getMonitorId()).isEqualTo(monitorId);
        assertThat(alert.getRuleId()).isEqualTo(ruleId);
    }
}