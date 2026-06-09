package com.aab.homehub.shopping;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShoppingRepository extends JpaRepository<ShoppingItem, UUID> {

    // get all items for a family grouped by category
    List<ShoppingItem> findByFamilyGroupIdOrderByCategoryAsc(UUID familyGroupId);

    // get by category — used for filtered view
    List<ShoppingItem> findByFamilyGroupIdAndCategory(
            UUID familyGroupId, ShoppingCategory category);

    // get only unpurchased items
    List<ShoppingItem> findByFamilyGroupIdAndPurchased(
            UUID familyGroupId, boolean purchased);

    // get by category and purchased status
    List<ShoppingItem> findByFamilyGroupIdAndCategoryAndPurchased(
            UUID familyGroupId, ShoppingCategory category, boolean purchased);

    // clear all purchased items — useful "clear basket" action
    void deleteByFamilyGroupIdAndPurchasedTrue(UUID familyGroupId);

    // used by AI service later
    long countByFamilyGroupIdAndPurchasedFalse(UUID familyGroupId);
}
