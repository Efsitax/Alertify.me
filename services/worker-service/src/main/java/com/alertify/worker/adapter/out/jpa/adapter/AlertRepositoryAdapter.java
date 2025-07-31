package com.alertify.worker.adapter.out.jpa.adapter;

import com.alertify.worker.adapter.out.jpa.entity.AlertJpaEntity;
import com.alertify.worker.adapter.out.jpa.repository.AlertJpaRepository;
import com.alertify.worker.domain.entity.Alert;
import com.alertify.worker.domain.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AlertRepositoryAdapter implements AlertRepository {

    private final AlertJpaRepository repository;

    @Override
    public Alert save(Alert alert) {
        AlertJpaEntity entity = new AlertJpaEntity();
        entity.setId(alert.getId() != null ? alert.getId() : UUID.randomUUID());
        entity.setMonitorId(alert.getMonitorId());
        entity.setRuleId(alert.getRuleId());
        entity.setFiredAt(alert.getFiredAt());
        entity.setMessage(alert.getMessage());

        AlertJpaEntity saved = repository.save(entity);
        return mapToDomain(saved);
    }

    private Alert mapToDomain(AlertJpaEntity entity) {
        return Alert.builder()
                .id(entity.getId())
                .monitorId(entity.getMonitorId())
                .ruleId(entity.getRuleId())
                .firedAt(entity.getFiredAt())
                .message(entity.getMessage())
                .build();
    }
}