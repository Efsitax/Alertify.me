package com.alertify.monitorservice.scheduler;

import com.alertify.monitorservice.domain.entity.Alert;
import com.alertify.monitorservice.domain.entity.Monitor;
import com.alertify.monitorservice.domain.entity.Rule;
import com.alertify.monitorservice.domain.entity.Snapshot;
import com.alertify.monitorservice.domain.repository.AlertRepository;
import com.alertify.monitorservice.domain.repository.SnapshotRepository;
import com.alertify.monitorservice.scheduler.model.MetricSample;
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

    public void evaluateAndNotify(Monitor monitor, MetricSample sample) {
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
                .orElse(null);

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
                        .id(java.util.UUID.randomUUID())
                        .monitorId(monitor.getId())
                        .ruleId(rule.getId())
                        .message(message)
                        .firedAt(Instant.now())
                        .build();
                alertRepository.save(alert);

                log.info("ALERT for monitor {} - {}", monitor.getId(), message);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseConfig(Object config) {
        if(config instanceof Map<?, ?> map) {
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
