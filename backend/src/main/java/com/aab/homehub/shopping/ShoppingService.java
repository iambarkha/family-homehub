package com.aab.homehub.shopping;

import com.aab.homehub.auth.entity.User;
import com.aab.homehub.exception.ResourceNotFoundException;
import com.aab.homehub.family.FamilyGroup;
import com.aab.homehub.shopping.dto.ShoppingRequest;
import com.aab.homehub.shopping.dto.ShoppingResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ShoppingService {

    private final ShoppingRepository shoppingRepo;
    private final ShoppingMapper shoppingMapper;

    // ADD single item
    public ShoppingResponse addItem(ShoppingRequest request) {
        FamilyGroup familyGroup = getCurrentUserFamily();
        User currentUser = getCurrentUser();

        ShoppingItem item = ShoppingItem.builder()
                .name(request.name())
                .quantity(request.quantity())
                .category(request.category())
                .familyGroup(familyGroup)
                .addedBy(currentUser)
                .build();

        return shoppingMapper.toResponse(shoppingRepo.save(item));
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


    // ADD multiple items at once — used by AI suggestion later
    public List<ShoppingResponse> addItems(List<ShoppingRequest> requests) {
        FamilyGroup familyGroup = getCurrentUserFamily();
        User currentUser = getCurrentUser();

        List<ShoppingItem> items = requests.stream()
                .map(request -> ShoppingItem.builder()
                        .name(request.name())
                        .quantity(request.quantity())
                        .category(request.category())
                        .familyGroup(familyGroup)
                        .addedBy(currentUser)
                        .build())
                .toList();

        return shoppingRepo.saveAll(items)
                .stream().map(shoppingMapper::toResponse).toList();
    }

    // GET all items grouped by category
    @Transactional(readOnly = true)
    public Map<ShoppingCategory, List<ShoppingResponse>> getItemsGroupedByCategory() {
        FamilyGroup familyGroup = getCurrentUserFamily();

        return shoppingRepo
                .findByFamilyGroupIdOrderByCategoryAsc(familyGroup.getId())
                .stream()
                .map(shoppingMapper::toResponse)
                .collect(Collectors.groupingBy(ShoppingResponse::category));
    }

    // GET items by category
    @Transactional(readOnly = true)
    public List<ShoppingResponse> getItemsByCategory(ShoppingCategory category) {
        FamilyGroup familyGroup = getCurrentUserFamily();
        return shoppingRepo
                .findByFamilyGroupIdAndCategory(familyGroup.getId(), category)
                .stream().map(shoppingMapper::toResponse).toList();
    }

    // TOGGLE purchased status
    public ShoppingResponse togglePurchased(UUID id) {
        ShoppingItem item = shoppingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
        item.setPurchased(!item.isPurchased());
        return shoppingMapper.toResponse(shoppingRepo.save(item));
    }

    // UPDATE item details
    public ShoppingResponse updateItem(UUID id, ShoppingRequest request) {
        ShoppingItem item = shoppingRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Item not found: " + id));
        item.setName(request.name());
        item.setQuantity(request.quantity());
        item.setCategory(request.category());
        return shoppingMapper.toResponse(shoppingRepo.save(item));
    }

    // DELETE single item
    public void deleteItem(UUID id) {
        if (!shoppingRepo.existsById(id)) {
            throw new ResourceNotFoundException("Item not found: " + id);
        }
        shoppingRepo.deleteById(id);
    }

    // DELETE all purchased items — clear basket
    public void clearPurchased() {
        FamilyGroup familyGroup = getCurrentUserFamily();
        shoppingRepo.deleteByFamilyGroupIdAndPurchasedTrue(familyGroup.getId());
    }
}
