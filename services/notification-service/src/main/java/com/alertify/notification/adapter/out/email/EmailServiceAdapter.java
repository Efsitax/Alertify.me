package com.alertify.notification.adapter.out.email;

import com.alertify.notification.domain.model.EmailNotification;
import com.alertify.notification.domain.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceAdapter implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendAlert(EmailNotification notification) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(notification.getRecipient());
            message.setSubject(notification.getSubject());
            message.setText(notification.getBody());

            mailSender.send(message);

            log.info("Alert email sent successfully to: {}", notification.getRecipient());
        } catch (MailException e) {
            log.error("Failed to send alert email to {}: {}", notification.getRecipient(), e.getMessage());
            throw new RuntimeException("Email sending failed", e);
        }
    }

    @Override
    public void sendTestEmail(String to, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(body);

            mailSender.send(message);

            log.info("Test email sent successfully to: {}", to);
        } catch (MailException e) {
            log.error("Failed to send test email to {}: {}", to, e.getMessage());
            throw new RuntimeException("Test email sending failed", e);
        }
    }

    @Override
    public boolean isEmailServiceAvailable() {
        try {
            return fromEmail != null && !fromEmail.trim().isEmpty();
        } catch (Exception e) {
            log.warn("Email service not available: {}", e.getMessage());
            return false;
        }
    }
}
