package com.aab.homehub.shopping;

import jakarta.persistence.*;
import lombok.*;
import com.aab.homehub.auth.entity.User;
import com.aab.homehub.family.FamilyGroup;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name="shopping_items")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ShoppingItem {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    private String quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ShoppingCategory category;

    @Column(nullable = false)
    private boolean purchased;

    // true when added by AI, false when added manually
    @Column(nullable = false)
    private boolean aiSuggested;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "added_by_id")
    private User addedBy;

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
        purchased = false;
        aiSuggested = false;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

}
