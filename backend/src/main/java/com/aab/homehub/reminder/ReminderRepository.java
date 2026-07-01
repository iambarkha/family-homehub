package com.aab.homehub.reminder;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder,UUID> {


    // get upcoming reminders — not yet sent
    List<Reminder> findByFamilyGroupIdAndSentFalseOrderByTriggerAtAsc(UUID familyGroupId);

    List<Reminder> findBySentFalseAndTriggerAtBefore(LocalDateTime now);
    @Query("SELECT r FROM Reminder r " +
            "JOIN FETCH r.assignedTo u " +
            "JOIN FETCH r.familyGroup fg " +
            "WHERE r.sent = false AND r.triggerAt < :now")
    List<Reminder> findDueReminders(@Param("now") LocalDateTime now);
}
