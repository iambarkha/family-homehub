package com.aab.homehub.nutrition;

import com.aab.homehub.family.FamilyGroup;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "nutrition_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NutritionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private LocalDate logDate;

    @Column(nullable = false)
    private Integer totalCalories;

    @Column(nullable = false)
    private Integer totalProteinGrams;

    private Integer totalCarbsGrams;

    private Integer totalFatGrams;

    // prevents duplicate alert emails from scheduler
    @Column(nullable = false)
    private boolean alertSent;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "family_group_id", nullable = false)
    private FamilyGroup familyGroup;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        alertSent = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}