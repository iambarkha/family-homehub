package com.aab.homehub.reminder.dto;

import com.aab.homehub.reminder.RecurrencePattern;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record ReminderRequest(
        @NotBlank(message = "Title is required")
        String title,

        String message,

        @NotNull(message = "Trigger time is required")
        LocalDateTime triggerAt,

        RecurrencePattern recurrencePattern,  // defaults to NONE if not provided

        String assignedToUserId               // optional — defaults to current user
) {}
