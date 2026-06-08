package com.aab.homehub.todo.dto;

import com.aab.homehub.todo.Priority;
import com.aab.homehub.todo.TodoScope;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record TodoRequest (@NotBlank(message = "Title is required")
                           String title,
                           String description,
                           @NotNull Priority priority,
                           @NotNull TodoScope scope,
                           LocalDate dueDate,
                           UUID assignedToUserId  )
{}
