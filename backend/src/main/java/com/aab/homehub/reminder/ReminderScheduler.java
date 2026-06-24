package com.aab.homehub.reminder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderScheduler {

    private final ReminderRepository reminderRepository;
    private final ReminderService reminderService;
    private final JavaMailSender mailSender;

    // runs every 60 seconds
    @Scheduled(fixedDelay = 60_000)
    public void processDueReminders() {
        List<Reminder> due = reminderRepository
                .findBySentFalseAndTriggerAtBefore(LocalDateTime.now());

        if (due.isEmpty()) return;

        log.info("Processing {} due reminders", due.size());

        due.forEach(reminder -> {
            try {
                sendEmail(reminder);
                reminderService.processReminder(reminder);
            } catch (Exception e) {
                log.error("Failed to process reminder {}: {}",
                        reminder.getId(), e.getMessage());
            }
        });
    }

    private void sendEmail(Reminder reminder) {
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(reminder.getAssignedTo().getEmail());
            msg.setSubject("Reminder: " + reminder.getTitle());
            msg.setText(reminder.getMessage() != null
                    ? reminder.getMessage()
                    : reminder.getTitle());
            mailSender.send(msg);
            log.info("Email sent to {} for reminder: {}",
                    reminder.getAssignedTo().getEmail(), reminder.getTitle());
        } catch (Exception e) {
            log.error("Failed to send email for reminder {}: {}",
                    reminder.getId(), e.getMessage());
        }
    }
}
