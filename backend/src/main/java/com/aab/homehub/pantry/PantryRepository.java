package com.aab.homehub.pantry;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PantryRepository extends JpaRepository<PantryItem, UUID> {

    // get all items for a family
    List<PantryItem> findByFamilyGroupIdOrderByNameAsc(UUID familyGroupId);

    // get items below threshold — used by scheduler
    @Query("SELECT p FROM PantryItem p WHERE p.familyGroup.id = :familyGroupId " +
            "AND p.currentQuantity < p.thresholdQuantity")
    List<PantryItem> findLowStockItems(@Param("familyGroupId") UUID familyGroupId);

    // get tracked items — oil, sugar etc — used by AI consumption analysis
    List<PantryItem> findByFamilyGroupIdAndTrackConsumptionTrue(UUID familyGroupId);

    // used by AI service for pantry-aware shopping suggestions
    List<PantryItem> findByFamilyGroupId(UUID familyGroupId);

    // used by AI chat context
    long countByFamilyGroupIdAndCurrentQuantityLessThan(
            UUID familyGroupId, Double threshold);

    // find specific item by name — used by consumption analysis
    Optional<PantryItem> findByFamilyGroupIdAndNameIgnoreCase(
            UUID familyGroupId, String name);
}