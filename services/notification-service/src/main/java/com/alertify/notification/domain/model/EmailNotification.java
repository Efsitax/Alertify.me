package com.alertify.notification.domain.model;

import com.alertify.common.domain.event.AlertEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmailNotification {

    private UUID notificationId;
    private String recipient;
    private String subject;
    private String body;
    private NotificationType type;
    private NotificationPriority priority;
    private Instant createdAt;

    // Alert specific fields
    private UUID alertId;
    private UUID monitorId;
    private UUID ruleId;

    public static EmailNotification fromAlert(AlertEvent alertEvent, String recipient) {
        return EmailNotification.builder()
                .notificationId(UUID.randomUUID())
                .recipient(recipient)
                .subject(buildSubject(alertEvent))
                .body(buildBody(alertEvent))
                .type(NotificationType.ALERT)
                .priority(NotificationPriority.HIGH)
                .createdAt(Instant.now())
                .alertId(alertEvent.getAlertId())
                .monitorId(alertEvent.getMonitorId())
                .ruleId(alertEvent.getRuleId())
                .build();
    }

    private static String buildSubject(AlertEvent alertEvent) {
        return String.format("🚨 Alert Triggered - Monitor %s",
                alertEvent.getMonitorId().toString().substring(0, 8));
    }

    private static String buildBody(AlertEvent alertEvent) {
        return String.format("""
                Dear User,
                
                An alert has been triggered for your monitor:
                
                📊 Alert Details:
                • Alert ID: %s
                • Monitor ID: %s
                • Rule ID: %s
                • Message: %s
                • Triggered At: %s
                
                📋 Description:
                %s
                
                Please review your monitor configuration if needed.
                
                Best regards,
                Alertify Team
                """,
                alertEvent.getAlertId(),
                alertEvent.getMonitorId(),
                alertEvent.getRuleId(),
                alertEvent.getMessage(),
                alertEvent.getFiredAt(),
                alertEvent.getMessage()
        );
    }
}
