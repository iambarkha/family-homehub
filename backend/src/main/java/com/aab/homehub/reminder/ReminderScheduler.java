package com.aab.homehub.reminder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    //private final JavaMailSender mailSender;
    private final ReminderEventProducer reminderEventProducer;


    // runs every 60 seconds
    @Scheduled(fixedDelay = 60_000)
    public void processDueReminders() {
        List<Reminder> due = reminderRepository.findDueReminders(LocalDateTime.now());

        if (due.isEmpty()) return;

        log.info("Processing {} due reminders", due.size());

        due.forEach(reminder -> {
            try {
                // publish to Kafka instead of sending email directly
                reminderEventProducer.publishReminderTriggered(
                        new ReminderTriggeredEvent(
                                reminder.getId().toString(),
                                reminder.getFamilyGroup().getId().toString(),
                                reminder.getAssignedTo().getEmail(),
                                reminder.getTitle(),
                                reminder.getMessage()
                        )
                );
                // mark as sent + handle recurrence
                reminderService.processReminder(reminder);

            } catch (Exception e) {
                log.error("Failed to process reminder {}: {}",
                        reminder.getId(), e.getMessage());
            }
        });
    }
}
