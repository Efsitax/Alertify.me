package com.alertify.worker.application.service;

import com.alertify.worker.adapter.out.kafka.AlertEventPublisher;
import com.alertify.worker.domain.entity.Alert;
import com.alertify.worker.domain.entity.Monitor;
import com.alertify.worker.domain.entity.Rule;
import com.alertify.worker.domain.entity.Snapshot;
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
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class RuleEvaluator {

    private final SnapshotRepository snapshotRepository;
    private final AlertRepository alertRepository;
    private final AlertEventPublisher alertEventPublisher;

    public void evaluateAndProcess(Monitor monitor, MetricSample sample) {
        // Save current snapshot first
        Snapshot snapshot = Snapshot.builder()
                .id(UUID.randomUUID())
                .monitorId(monitor.getId())
                .metric(sample.metric())
                .value(sample.value())
                .unit(sample.unit())
                .at(sample.at())
                .build();
        snapshotRepository.save(snapshot);
        log.debug("Saved snapshot for monitor {}: {} {}", monitor.getId(), sample.value(), sample.unit());

        // Get previous snapshot (excluding current one)
        Optional<Snapshot> previousSnapshotOpt = snapshotRepository.findLastByMonitorId(monitor.getId())
                .filter(s -> !s.getAt().equals(sample.at()));

        List<Rule> rules = monitor.getRules() != null ? monitor.getRules() : List.of();
        log.debug("Evaluating {} rules for monitor {}", rules.size(), monitor.getId());

        for (Rule rule : rules) {
            boolean triggered = evaluateRule(rule, sample, previousSnapshotOpt);

            if (triggered) {
                String message = createAlertMessage(rule, sample, previousSnapshotOpt);
                fireAlert(monitor, rule, message);
            }
        }
    }

    private boolean evaluateRule(Rule rule, MetricSample sample, Optional<Snapshot> previousSnapshotOpt) {
        Map<String, Object> config = rule.getConfig() != null ? parseConfig(rule.getConfig()) : Map.of();

        switch (rule.getType().toUpperCase()) {
            case "TARGET_PRICE" -> {
                BigDecimal target = getBigDecimal(config.get("targetPrice"));
                if (target != null && sample.value().compareTo(target) <= 0) {
                    log.info("TARGET_PRICE rule triggered: {} <= {}", sample.value(), target);
                    return true;
                }
            }

            case "PERCENT_DROP" -> {
                if (previousSnapshotOpt.isEmpty()) {
                    log.debug("PERCENT_DROP rule skipped: no previous snapshot available for monitor {}",
                            sample.at());
                    return false;
                }

                Snapshot previousSnapshot = previousSnapshotOpt.get();
                BigDecimal percent = getBigDecimal(config.get("percent"));

                if (percent != null && previousSnapshot.getValue().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal diff = previousSnapshot.getValue()
                            .subtract(sample.value())
                            .divide(previousSnapshot.getValue(), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));

                    if (diff.compareTo(percent) >= 0) {
                        log.info("PERCENT_DROP rule triggered: {} dropped by {}% (threshold: {}%)",
                                sample.value(), diff, percent);
                        return true;
                    }
                }
            }

            default -> log.warn("Unknown rule type: {}", rule.getType());
        }

        return false;
    }

    private String createAlertMessage(Rule rule, MetricSample sample, Optional<Snapshot> previousSnapshotOpt) {
        Map<String, Object> config = parseConfig(rule.getConfig());

        return switch (rule.getType().toUpperCase()) {
            case "TARGET_PRICE" -> {
                BigDecimal target = getBigDecimal(config.get("targetPrice"));
                yield String.format("Price reached target: %s %s (target: %s)",
                        sample.value(), sample.unit(), target);
            }

            case "PERCENT_DROP" -> {
                if (previousSnapshotOpt.isPresent()) {
                    Snapshot previous = previousSnapshotOpt.get();
                    BigDecimal diff = previous.getValue()
                            .subtract(sample.value())
                            .divide(previous.getValue(), 2, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100));
                    yield String.format("Price dropped %.2f%% from %s to %s %s",
                            diff, previous.getValue(), sample.value(), sample.unit());
                } else {
                    yield "Price drop detected (no previous data)";
                }
            }

            default -> String.format("Rule %s triggered for value %s %s",
                    rule.getType(), sample.value(), sample.unit());
        };
    }

    private void fireAlert(Monitor monitor, Rule rule, String message) {
        Alert alert = Alert.builder()
                .id(UUID.randomUUID())
                .monitorId(monitor.getId())
                .ruleId(rule.getId())
                .firedAt(Instant.now())
                .message(message)
                .build();

        Alert saved = alertRepository.save(alert);
        log.info("ALERT FIRED for monitor {}: {}", monitor.getId(), message);

        try {
            alertEventPublisher.publish(AlertEvent.builder()
                    .alertId(saved.getId())
                    .monitorId(saved.getMonitorId())
                    .ruleId(saved.getRuleId())
                    .firedAt(saved.getFiredAt())
                    .message(saved.getMessage())
                    .build());

            log.info("Alert event published to Kafka for monitor {}", monitor.getId());
        } catch (Exception e) {
            log.error("Failed to publish alert event for monitor {}: {}", monitor.getId(), e.getMessage(), e);
            throw new KafkaPublishException("Failed to publish alert event for monitor " + monitor.getId(), e);
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