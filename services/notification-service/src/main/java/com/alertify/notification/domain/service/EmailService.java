package com.alertify.notification.domain.service;

import com.alertify.notification.domain.model.EmailNotification;

public interface EmailService {
    void sendAlert(EmailNotification notification);
    void sendTestEmail(String to, String subject, String body);
    boolean isEmailServiceAvailable();
}
