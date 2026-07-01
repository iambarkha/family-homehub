package com.aab.homehub.reminder;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReminderTriggeredEvent {
    private String reminderId;
    private String familyGroupId;
    private String assignedToEmail;
    private String title;
    private String message;
}
