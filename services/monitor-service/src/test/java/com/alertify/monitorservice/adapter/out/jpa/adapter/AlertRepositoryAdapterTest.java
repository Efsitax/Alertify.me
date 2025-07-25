package com.alertify.monitorservice.adapter.out.jpa.adapter;

import com.alertify.monitorservice.BaseIntegrationTest;
import com.alertify.monitorservice.domain.entity.Alert;
import com.alertify.monitorservice.domain.repository.AlertRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AlertRepositoryAdapterTest extends BaseIntegrationTest {

    @Autowired
    private AlertRepository alertRepository;

    @Test
    void saveAlert() {
        UUID monitorId = UUID.randomUUID();
        UUID ruleId = UUID.randomUUID();

        Alert alert = Alert.builder()
                .monitorId(monitorId)
                .ruleId(ruleId)
                .message("Price dropped 10%")
                .firedAt(Instant.now())
                .build();

        Alert saved = alertRepository.save(alert);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getMonitorId()).isEqualTo(monitorId);
        assertThat(saved.getRuleId()).isEqualTo(ruleId);
        assertThat(saved.getMessage()).isEqualTo("Price dropped 10%");
    }
}