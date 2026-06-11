package com.aab.homehub.pantry;

import com.aab.homehub.family.FamilyGroup;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "pantry_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class PantryItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Double currentQuantity;

    @Column(nullable = false)
    private String unit;               // "litres", "kg", "pcs", "grams"

    @Column(nullable = false)
    private Double thresholdQuantity;  // alert when below this

    private Double averageWeeklyUsage; // calculated over time by AI

    private LocalDate lastRestockedAt;

    // true for oil, sugar, milk — items worth tracking closely
    @Column(nullable = false)
    private boolean trackConsumption;

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
        trackConsumption = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
