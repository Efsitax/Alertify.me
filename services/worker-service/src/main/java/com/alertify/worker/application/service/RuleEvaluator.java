package com.alertify.worker.application.service;

import com.alertify.worker.adapter.out.kafka.AlertEventPublisher;
import com.alertify.worker.domain.entity.Alert;
import com.alertify.worker.domain.entity.Monitor;
import com.alertify.worker.domain.entity.Rule;
import com.alertify.worker.domain.entity.Snapshot;
import com.alertify.worker.domain.exception.SnapshotNotFoundException;
import com.alertify.worker.domain.model.AlertEvent;
import com.alertify.worker.domain.repository.AlertRepository;
import com.alertify.worker.domain.repository.SnapshotRepository;
import com.alertify.worker.domain.model.MetricSample;
import com.alertify.worker.domain.exception.KafkaPublishException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEvaluator {

    private final SnapshotRepository snapshotRepository;
    private final AlertRepository alertRepository;
    private final AlertEventPublisher alertEventPublisher;

    public void evaluateAndProcess(Monitor monitor, MetricSample sample) {
        Snapshot snapshot = Snapshot.builder()
                .id(UUID.randomUUID())
                .monitorId(monitor.getId())
                .metric(sample.metric())
                .value(sample.value())
                .unit(sample.unit())
                .at(sample.at())
                .build();
        snapshotRepository.save(snapshot);

        var previousSnapshot = snapshotRepository.findLastByMonitorId(monitor.getId())
                .filter(s -> !s.getAt().equals(sample.at()))
                .orElseThrow(() -> new SnapshotNotFoundException(monitor.getId()));

        List<Rule> rules = monitor.getRules() != null ? monitor.getRules() : List.of();
        for (Rule rule : rules) {
            boolean triggered = false;
            String message = "";

            Map<String, Object> config = rule.getConfig() != null ? parseConfig(rule.getConfig()) : Map.of();

            if ("TARGET_PRICE".equalsIgnoreCase(rule.getType())) {
                BigDecimal target = getBigDecimal(config.get("targetPrice"));
                if (target != null && sample.value().compareTo(target) <= 0) {
                    triggered = true;
                    message = "Price reached target: " + sample.value();
                }
            }

            if ("PERCENT_DROP".equalsIgnoreCase(rule.getType()) && previousSnapshot != null) {
                BigDecimal percent = getBigDecimal(config.get("percent"));
                if (percent != null && previousSnapshot.getValue().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal diff = previousSnapshot.getValue()
                            .subtract(sample.value())
                            .divide(previousSnapshot.getValue(), 2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    if (diff.compareTo(percent) >= 0) {
                        triggered = true;
                        message = "Price dropped " + diff + "% since last snapshot.";
                    }
                }
            }

            if (triggered) {
                Alert alert = Alert.builder()
                        .id(UUID.randomUUID())
                        .monitorId(monitor.getId())
                        .ruleId(rule.getId())
                        .firedAt(Instant.now())
                        .message(message)
                        .build();
                Alert saved = alertRepository.save(alert);

                try {
                    alertEventPublisher.publish(AlertEvent.builder()
                            .alertId(saved.getId())
                            .monitorId(saved.getMonitorId())
                            .ruleId(saved.getRuleId())
                            .firedAt(saved.getFiredAt())
                            .message(saved.getMessage())
                            .build());

                    log.info("ALERT for monitor {} - {}", monitor.getId(), message);
                } catch (Exception e) {
                    log.error("Failed to publish alert event for monitor {}: {}", monitor.getId(), e.getMessage(), e);
                    throw new KafkaPublishException("Failed to publish alert event for monitor " + monitor.getId(), e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfig(Object config) {
        if (config instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    private BigDecimal getBigDecimal(Object value) {
        if (value instanceof Number num) {
            return BigDecimal.valueOf(num.doubleValue());
        }
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (Exception e) {
            return null;
        }
    }
}