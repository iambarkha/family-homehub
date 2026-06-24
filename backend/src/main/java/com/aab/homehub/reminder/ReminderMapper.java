package com.aab.homehub.reminder;

import com.aab.homehub.reminder.dto.ReminderRequest;
import com.aab.homehub.reminder.dto.ReminderResponse;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ReminderMapper {

        ReminderResponse toResponse(Reminder reminder);

        Reminder toEntity(ReminderRequest request);
}
