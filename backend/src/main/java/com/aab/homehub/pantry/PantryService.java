package com.aab.homehub.pantry;

import com.aab.homehub.auth.entity.User;
import com.aab.homehub.exception.ResourceNotFoundException;
import com.aab.homehub.family.FamilyGroup;
import com.aab.homehub.pantry.dto.PantryRequest;
import com.aab.homehub.pantry.dto.PantryResponse;
import com.aab.homehub.pantry.dto.PantryUpdateQuantityRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class PantryService {

    private final PantryRepository pantryRepository;



    public PantryResponse addItem(PantryRequest request) {

        FamilyGroup familyGroup = getCurrentUserFamily();
        //prevent duplicates by name within family
        pantryRepository.findByFamilyGroupIdAndNameIgnoreCase(familyGroup.getId(), request.name())
                .ifPresent(item -> {
                    throw new IllegalArgumentException("Item with name '" + request.name() + "' already exists in your pantry");
                });
        PantryItem pantryItem = PantryItem.builder()
                .name(request.name())
                .currentQuantity(request.currentQuantity())
                .thresholdQuantity(request.thresholdQuantity())
                .unit(request.unit())
                .trackConsumption(request.trackConsumption())
                .familyGroup(familyGroup)
                .build();
        return toResponse(pantryRepository.save(pantryItem));
    }

    // maps entity to response — sets lowStock flag dynamically
    private PantryResponse toResponse(PantryItem item) {
        return new PantryResponse(
                item.getId(),
                item.getName(),
                item.getCurrentQuantity(),
                item.getUnit(),
                item.getThresholdQuantity(),
                item.getAverageWeeklyUsage(),
                item.getLastRestockedAt(),
                item.isTrackConsumption(),
                item.getCurrentQuantity() < item.getThresholdQuantity(), // lowStock flag
                item.getCreatedAt()
        );
    }

    private User getCurrentUser() {
        User currentUser = (User) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if(currentUser == null) {
            throw new IllegalStateException("Current user is null");
        }
        return currentUser;
    }

    private FamilyGroup getCurrentUserFamily() {

        User currentUser = getCurrentUser();
        FamilyGroup familyGroup = currentUser.getFamilyGroup();
        if (familyGroup == null) {
            throw new IllegalStateException("User does not belong to a family");
        }
        return familyGroup;
    }

    @Transactional(readOnly = true)
    public List<PantryResponse> getLowStockItems() {
        FamilyGroup familyGroup = getCurrentUserFamily();
        return pantryRepository.findLowStockItems(familyGroup.getId())
                .stream().map(this::toResponse).toList();
    }

    // called by PantryScheduler daily
    @Transactional(readOnly = true)
    public List<PantryItem> getLowStockItemsForScheduler(UUID familyGroupId) {
        return pantryRepository.findLowStockItems(familyGroupId);
    }

    //Get all items
    @Transactional(readOnly = true)
    public List<PantryResponse> getAllItems() {
        FamilyGroup familyGroup = getCurrentUserFamily();
        return pantryRepository.findByFamilyGroupIdOrderByNameAsc(familyGroup.getId())
                .stream().map(this::toResponse).toList();
    }

    public PantryResponse updateQuantity(UUID id, PantryUpdateQuantityRequest request) {

        PantryItem item = pantryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pantry item not found: " + id));

        // if restocking — update lastRestockedAt
        if (request.restocked()) {
            item.setLastRestockedAt(LocalDate.now());
            log.info("Pantry item restocked: {} → {} {}",
                    item.getName(), request.currentQuantity(), item.getUnit());
        }

        // calculate average weekly usage when consuming
        if (!request.restocked() && item.getAverageWeeklyUsage() == null) {
            // first consumption record — set as initial average
            double consumed = item.getCurrentQuantity() - request.currentQuantity();
            if (consumed > 0) {
                item.setAverageWeeklyUsage(consumed);
            }
        } else if (!request.restocked() && item.getAverageWeeklyUsage() != null) {
            // rolling average — (existing avg + new consumption) / 2
            double consumed = item.getCurrentQuantity() - request.currentQuantity();
            if (consumed > 0) {
                double newAvg = (item.getAverageWeeklyUsage() + consumed) / 2;
                item.setAverageWeeklyUsage(newAvg);
            }
        }

        item.setCurrentQuantity(request.currentQuantity());
        return toResponse(pantryRepository.save(item));
    }

    // UPDATE item details
    public PantryResponse updateItem(UUID id, PantryRequest request) {
        PantryItem item = pantryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Pantry item not found: " + id));

        item.setName(request.name());
        item.setUnit(request.unit());
        item.setThresholdQuantity(request.thresholdQuantity());
        item.setTrackConsumption(request.trackConsumption());

        return toResponse(pantryRepository.save(item));
    }

    // DELETE item
    public void deleteItem(UUID id) {
        if (!pantryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Pantry item not found: " + id);
        }
        pantryRepository.deleteById(id);
    }
}
