package com.aab.homehub.reminder.dto;

import com.aab.homehub.reminder.RecurrencePattern;

import java.time.LocalDateTime;
import java.util.UUID;

public record ReminderResponse(
        UUID id,
        String title,
        String message,
        LocalDateTime triggerAt,
        boolean sent,
        RecurrencePattern recurrencePattern,
        String assignedToName,
        LocalDateTime createdAt
) {}
