package com.aab.homehub.todo.dto;

import com.aab.homehub.todo.Priority;
import com.aab.homehub.todo.TodoScope;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record TodoResponse(UUID id,
                           String title,
                           String description,
                           Priority priority,
                           TodoScope scope,
                           LocalDate dueDate,
                           boolean completed,
                           String assignedToName,
                           LocalDateTime createdAt) {
}
