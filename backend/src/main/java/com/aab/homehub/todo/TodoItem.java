package com.aab.homehub.todo;

import com.aab.homehub.auth.entity.User;
import com.aab.homehub.family.FamilyGroup;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="todo_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoItem {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String description;

    @Enumerated(EnumType.STRING)
    private Priority priority;  // HIGH, MEDIUM, LOW

    @Enumerated(EnumType.STRING)
    private TodoScope scope;    // WEEKLY, MONTHLY, LONGTERM

    private LocalDate dueDate;

    private boolean completed;

    @ManyToOne(fetch = FetchType.LAZY)
    private User assignedTo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id")
    private FamilyGroup familyGroup;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        completed = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
