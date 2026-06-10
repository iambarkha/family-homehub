package com.aab.homehub.reminder;

import com.aab.homehub.auth.UserRepository;
import com.aab.homehub.auth.entity.User;
import com.aab.homehub.exception.ResourceNotFoundException;
import com.aab.homehub.family.FamilyGroup;
import com.aab.homehub.reminder.dto.ReminderRequest;
import com.aab.homehub.reminder.dto.ReminderResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final UserRepository userRepository;
    private final ReminderMapper reminderMapper;

    public ReminderResponse postReminder(ReminderRequest request) {
        if (request.triggerAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Trigger time must be in the future");
        }
        User currentUser = getCurrentUser();
        FamilyGroup familyGroup = currentUser.getFamilyGroup();
        //if assignedTo id is not provided then assign it to current user
        User assignedToUser = request.assignedToUserId() != null ?
                userRepository.findById(UUID.fromString(request.assignedToUserId()))
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Assigned user not found" + request.assignedToUserId()))
                : currentUser;

       Reminder reminder = Reminder.builder()
               .title(request.title())
               .message(request.message())
               .triggerAt(request.triggerAt())
               .recurrencePattern(
                       request.recurrencePattern() != null
                               ? request.recurrencePattern()
                               : RecurrencePattern.NONE)
               .assignedTo(assignedToUser)
               .familyGroup(familyGroup)
               .build();

        return reminderMapper.toResponse(reminderRepository.save(reminder));
    }
    // TEST 8 — scheduler calls this → marks sent = true → creates next if recurring
    @Transactional
    public void processReminder(Reminder reminder) {

        // mark current reminder as sent
        reminder.setSent(true);
        reminderRepository.save(reminder);
        log.info("Reminder marked as sent: {}", reminder.getTitle());

        // if recurring — auto-create next occurrence
        if (reminder.getRecurrencePattern() != RecurrencePattern.NONE) {

            LocalDateTime nextTrigger = switch (reminder.getRecurrencePattern()) {
                case DAILY   -> reminder.getTriggerAt().plusDays(1);
                case WEEKLY  -> reminder.getTriggerAt().plusWeeks(1);
                case MONTHLY -> reminder.getTriggerAt().plusMonths(1);
                default      -> null;
            };

            if (nextTrigger != null) {
                Reminder next = Reminder.builder()
                        .title(reminder.getTitle())
                        .message(reminder.getMessage())
                        .triggerAt(nextTrigger)
                        .recurrencePattern(reminder.getRecurrencePattern())
                        .assignedTo(reminder.getAssignedTo())
                        .familyGroup(reminder.getFamilyGroup())
                        .build();

                reminderRepository.save(next);
                log.info("Next occurrence created for: {} at {}",
                        reminder.getTitle(), nextTrigger);
            }
        }
    }
    private User getCurrentUser() {
        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if(currentUser == null) {
            throw new IllegalStateException("Current user is null");
        }
        return currentUser;
    }

    private FamilyGroup getCurrentUserFamily() {

        User currentUser = getCurrentUser();
        FamilyGroup familyGroup = currentUser.getFamilyGroup();
        if (familyGroup == null) {
            throw new IllegalStateException("User does not belong to a family");
        }
        return familyGroup;
    }

    public List<ReminderResponse> getAllReminders() {
       return reminderRepository.findAll().stream()
                .filter(reminder -> reminder.getFamilyGroup().getId().equals(getCurrentUserFamily().getId()))
                .map(reminderMapper::toResponse)
                .toList();
    }

    public List<ReminderResponse> getUpcomingReminders() {
        return reminderRepository.findByFamilyGroupIdAndSentFalseOrderByTriggerAtAsc(getCurrentUserFamily().getId()).stream()
                .map(reminderMapper::toResponse)
                .toList();
    }

    public ReminderResponse updateReminder(UUID id, ReminderRequest request) {
        Reminder reminder = reminderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reminder not found: " + id));
        if (request.triggerAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Trigger time must be in the future");
        }
        reminder.setTitle(request.title());
        reminder.setMessage(request.message());
        reminder.setTriggerAt(request.triggerAt());
        reminder.setRecurrencePattern(
                request.recurrencePattern() != null
                        ? request.recurrencePattern()
                        : RecurrencePattern.NONE);

        return reminderMapper.toResponse(reminderRepository.save(reminder));

    }

    public void deleteReminder(UUID id) {
        if (!reminderRepository.existsById(id)) {
            throw new ResourceNotFoundException("Reminder not found: " + id);
        }
        reminderRepository.deleteById(id);
    }
}
