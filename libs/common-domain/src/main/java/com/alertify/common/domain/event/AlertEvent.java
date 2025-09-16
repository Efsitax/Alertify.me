package com.alertify.common.domain.event;

import java.time.Instant;
import java.util.UUID;
import java.util.Objects;

public class AlertEvent {
    private UUID alertId;
    private UUID monitorId;
    private UUID ruleId;
    private Instant firedAt;
    private String message;

    public AlertEvent() {}

    public AlertEvent(UUID alertId, UUID monitorId, UUID ruleId, Instant firedAt, String message) {
        this.alertId = alertId;
        this.monitorId = monitorId;
        this.ruleId = ruleId;
        this.firedAt = firedAt;
        this.message = message;
    }

    public static AlertEventBuilder builder() {
        return new AlertEventBuilder();
    }

    public UUID getAlertId() { return alertId; }
    public UUID getMonitorId() { return monitorId; }
    public UUID getRuleId() { return ruleId; }
    public Instant getFiredAt() { return firedAt; }
    public String getMessage() { return message; }

    public void setAlertId(UUID alertId) { this.alertId = alertId; }
    public void setMonitorId(UUID monitorId) { this.monitorId = monitorId; }
    public void setRuleId(UUID ruleId) { this.ruleId = ruleId; }
    public void setFiredAt(Instant firedAt) { this.firedAt = firedAt; }
    public void setMessage(String message) { this.message = message; }

    public static class AlertEventBuilder {
        private UUID alertId;
        private UUID monitorId;
        private UUID ruleId;
        private Instant firedAt;
        private String message;

        public AlertEventBuilder alertId(UUID alertId) { this.alertId = alertId; return this; }
        public AlertEventBuilder monitorId(UUID monitorId) { this.monitorId = monitorId; return this; }
        public AlertEventBuilder ruleId(UUID ruleId) { this.ruleId = ruleId; return this; }
        public AlertEventBuilder firedAt(Instant firedAt) { this.firedAt = firedAt; return this; }
        public AlertEventBuilder message(String message) { this.message = message; return this; }

        public AlertEvent build() {
            return new AlertEvent(alertId, monitorId, ruleId, firedAt, message);
        }
    }

    @Override
    public String toString() {
        return "AlertEvent{" +
                "alertId=" + alertId +
                ", monitorId=" + monitorId +
                ", ruleId=" + ruleId +
                ", firedAt=" + firedAt +
                ", message='" + message + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlertEvent that = (AlertEvent) o;
        return Objects.equals(alertId, that.alertId) &&
                Objects.equals(monitorId, that.monitorId) &&
                Objects.equals(ruleId, that.ruleId) &&
                Objects.equals(firedAt, that.firedAt) &&
                Objects.equals(message, that.message);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alertId, monitorId, ruleId, firedAt, message);
    }
}