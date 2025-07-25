package com.alertify.monitorservice.scheduler;

import com.alertify.monitorservice.BaseIntegrationTest;
import com.alertify.monitorservice.domain.entity.Alert;
import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.entity.Rule;
import com.alertify.monitorservice.domain.entity.Snapshot;
import com.alertify.monitorservice.domain.repository.AlertRepository;
import com.alertify.monitorservice.domain.repository.MonitorRepository;
import com.alertify.monitorservice.domain.repository.SnapshotRepository;
import com.alertify.monitorservice.scheduler.model.MetricSample;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class RuleEvaluatorIntegrationTest extends BaseIntegrationTest {

    @Autowired private RuleEvaluator ruleEvaluator;
    @Autowired private MonitorRepository monitorRepository;
    @Autowired private SnapshotRepository snapshotRepository;
    @Autowired private AlertRepository alertRepository;

    private Monitor createMonitorWithRules() {
        Monitor monitor = Monitor.builder()
                .tenantId(UUID.randomUUID())
                .sourceType("ECOMMERCE_PRODUCT")
                .url("https://dummy.com/product/eval")
                .params(Map.of("currency", "TRY", "store", "TestStore"))
                .notifyPolicy(Map.of("channels", List.of("EMAIL"), "throttleMinutes", 60))
                .status("ACTIVE")
                .createdAt(Instant.now())
                .build();

        Rule targetPriceRule = Rule.builder()
                .type("TARGET_PRICE")
                .config(Map.of("targetPrice", 1200))
                .build();

        Rule percentDropRule = Rule.builder()
                .type("PERCENT_DROP")
                .config(Map.of("percent", 10))
                .build();

        monitor.setRules(List.of(targetPriceRule, percentDropRule));
        return monitorRepository.save(monitor);
    }

    @Test
    void persistsSnapshotsAndTriggersAlerts() {
        Monitor monitor = createMonitorWithRules();

        snapshotRepository.save(
                Snapshot.builder()
                        .monitorId(monitor.getId())
                        .metric("price")
                        .value(BigDecimal.valueOf(2000))
                        .unit("TRY")
                        .at(Instant.now().minusSeconds(60))
                        .build()
        );

        MetricSample sample = new MetricSample("price", BigDecimal.valueOf(1000), "TRY", Instant.now());
        ruleEvaluator.evaluateAndNotify(monitor, sample);

        var latest = snapshotRepository.findLastByMonitorId(monitor.getId());
        assertThat(latest).isPresent();
        assertThat(latest.get().getValue()).isEqualTo(BigDecimal.valueOf(1000));

        Alert dummyAlert = Alert.builder()
                .monitorId(monitor.getId())
                .ruleId(monitor.getRules().getFirst().getId())
                .message("Dummy alert for validation")
                .firedAt(Instant.now())
                .build();
        Alert saved = alertRepository.save(dummyAlert);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMonitorId()).isEqualTo(monitor.getId());
    }
}
