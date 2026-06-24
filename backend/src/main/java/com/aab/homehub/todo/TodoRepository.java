package com.aab.homehub.todo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface TodoRepository extends JpaRepository<TodoItem, UUID> {
    @Query("SELECT t FROM TodoItem t WHERE t.familyGroup.id = :familyId")
    List<TodoItem> findAllByFamilyId(UUID familyId);

    @Query("SELECT t FROM TodoItem t WHERE t.familyGroup.id = :familyId AND t.scope = :scope")
    List<TodoItem> findAllByFamilyIdAndScope(UUID familyId, TodoScope scope);
}
