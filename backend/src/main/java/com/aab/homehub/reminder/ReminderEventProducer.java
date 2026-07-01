package com.aab.homehub.reminder;

import com.aab.homehub.config.KafkaTopicConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ReminderEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishReminderTriggered(ReminderTriggeredEvent event) {
        kafkaTemplate.send(KafkaTopicConfig.REMINDER_TRIGGERED,
                        event.getFamilyGroupId(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish reminder.triggered: {}",
                                ex.getMessage());
                    } else {
                        log.info("Published reminder.triggered: {}", event.getTitle());
                    }
                });
    }
}