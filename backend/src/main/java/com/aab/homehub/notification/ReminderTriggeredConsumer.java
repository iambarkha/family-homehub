package com.aab.homehub.notification;

import com.aab.homehub.config.KafkaTopicConfig;
import com.aab.homehub.reminder.ReminderTriggeredEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderTriggeredConsumer {
      private final JavaMailSender mailSender;
    @KafkaListener(
            topics = KafkaTopicConfig.REMINDER_TRIGGERED,
            groupId = "household-group"
    )
    public void onReminderTriggered(ReminderTriggeredEvent event) {
        log.info("Received reminder.triggered for: {}", event.getTitle());
        try {
            sendEmail(event);
        } catch (Exception e) {
            log.error("Failed to send reminder email: {}", e.getMessage());
        }
    }

    private void sendEmail(ReminderTriggeredEvent event) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(event.getAssignedToEmail());
            msg.setSubject("Reminder: " + event.getTitle());
            msg.setText(event.getMessage() != null
                    ? event.getMessage()
                    : event.getTitle());
            mailSender.send(msg);
            log.info("Email sent to {} for: {}",
                    event.getAssignedToEmail(), event.getTitle());
        } catch (Exception e) {
            log.error("Failed to send email for reminder {}: {}",
                    event.getReminderId(), e.getMessage());
        }
    }
}


