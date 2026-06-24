package com.aab.homehub.meal;

import com.aab.homehub.auth.entity.User;
import com.aab.homehub.family.FamilyGroup;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "meal_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    private CuisineType cuisineType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MealSlot slot;

    @Column(nullable = false)
    private String mealName;

    private String description;

    //@TODO
    // filled later by AI after plan is saved
    private Integer estimatedCalories;
    private Integer estimatedProteinGrams;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id", nullable = false)
    private FamilyGroup familyGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id")
    private User createdBy;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
