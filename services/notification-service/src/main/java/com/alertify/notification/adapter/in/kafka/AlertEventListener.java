package com.alertify.notification.adapter.in.kafka;

import com.alertify.common.domain.event.AlertEvent;
import com.alertify.notification.domain.model.EmailNotification;
import com.alertify.notification.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AlertEventListener {

    private final EmailService emailService;
    private final WebClient webClient;

    @Value("${notification.default.recipient:admin@alertify.com}")
    private String defaultRecipient;

    @Value("${services.monitor-service.url:http://localhost:8080}")
    private String monitorServiceUrl;

    @KafkaListener(topics = "alert.created", groupId = "notification-service")
    public void handleAlertEvent(AlertEvent alertEvent) {
        log.info("Received alert event: AlertID={}, MonitorID={}",
                alertEvent.getAlertId(), alertEvent.getMonitorId());

        try {
            String recipient = getRecipientForMonitor(alertEvent.getMonitorId());
            EmailNotification emailNotification = EmailNotification.fromAlert(alertEvent, recipient);

            log.info("Sending alert email to {}", defaultRecipient);
            emailService.sendAlert(emailNotification);
            log.info("Alert email sent successfully for AlertID={}", alertEvent.getAlertId());
        } catch (Exception e) {
            log.error("Failed to process alert event for AlertID={}: {}", alertEvent.getAlertId(), e.getMessage(), e);
            logAlertFallback(alertEvent);
        }
    }

    private String getRecipientForMonitor(UUID monitorId) {
        try {
            log.debug("Fetching monitor details for ID : {}", monitorId);

            @SuppressWarnings("unchecked")
            Map<String, Object> monitor = webClient.get()
                    .uri(monitorServiceUrl + "/api/monitors/" + monitorId)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .timeout(Duration.ofSeconds(5))
                    .block();

            if (monitor != null && monitor.get("notifyPolicy") instanceof Map<?,?> notifyPolicy) {
                Object email = notifyPolicy.get("email");
                if (email != null && !email.toString().trim().isEmpty()) {
                    log.debug("Found email in monitor notify policy: {}", email);
                    return email.toString();
                }
            }

            log.debug("No email found in monitor notify policy, using default recipient");
            return defaultRecipient;
        } catch (Exception e) {
            log.warn("Failed to fetch monitor details for {}, using default recipient: {}", monitorId, e.getMessage());
            return defaultRecipient;
        }
    }

    private void logAlertFallback(AlertEvent alertEvent) {
        log.warn("FALLBACK ALERT LOG - Email failed, logging alert details:");
        log.warn("   Alert ID: {}", alertEvent.getAlertId());
        log.warn("   Monitor ID: {}", alertEvent.getMonitorId());
        log.warn("   Rule ID: {}", alertEvent.getRuleId());
        log.warn("   Message: {}", alertEvent.getMessage());
        log.warn("   Fired At: {}", alertEvent.getFiredAt());
    }
}
