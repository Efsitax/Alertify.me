package com.alertify.worker.adapter.out.kafka;

import com.alertify.worker.domain.model.AlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventPublisher {

    private final KafkaTemplate<String, AlertEvent> kafkaTemplate;
    private static final String TOPIC = "alert.created";

    public void publish(AlertEvent event) {
        kafkaTemplate.send(TOPIC, event);
        log.info("Published alert event to Kafka: {}", event);
    }
}