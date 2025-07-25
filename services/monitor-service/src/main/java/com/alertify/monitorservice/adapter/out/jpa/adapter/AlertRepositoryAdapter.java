package com.alertify.monitorservice.adapter.out.jpa.adapter;

import com.alertify.monitorservice.adapter.out.jpa.entity.AlertJpaEntity;
import com.alertify.monitorservice.adapter.out.jpa.repository.AlertJpaRepository;
import com.alertify.monitorservice.domain.entity.Alert;
import com.alertify.monitorservice.domain.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Primary
@Component
@RequiredArgsConstructor
public class AlertRepositoryAdapter implements AlertRepository {

    private final AlertJpaRepository alertJpaRepository;

    @Override
    public Alert save(Alert alert) {
        AlertJpaEntity entity = new AlertJpaEntity();
        entity.setId(alert.getId() != null ? alert.getId() : UUID.randomUUID());
        entity.setMonitorId(alert.getMonitorId());
        entity.setRuleId(alert.getRuleId());
        entity.setFiredAt(alert.getFiredAt());
        entity.setMessage(alert.getMessage());

        AlertJpaEntity saved = alertJpaRepository.save(entity);
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
