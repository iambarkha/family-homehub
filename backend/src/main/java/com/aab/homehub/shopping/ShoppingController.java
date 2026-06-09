package com.aab.homehub.shopping;

import com.aab.homehub.shopping.dto.ShoppingRequest;
import com.aab.homehub.shopping.dto.ShoppingResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/shopping")
@RequiredArgsConstructor
public class ShoppingController {

    private final ShoppingService shoppingService;

    // ADD single item
    @PostMapping
    public ResponseEntity<ShoppingResponse> addItem(
            @Valid @RequestBody ShoppingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shoppingService.addItem(request));
    }

    // ADD multiple items at once
    @PostMapping("/batch")
    public ResponseEntity<List<ShoppingResponse>> addItems(
            @Valid @RequestBody List<ShoppingRequest> requests) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(shoppingService.addItems(requests));
    }

    // GET all items grouped by category
    @GetMapping
    public ResponseEntity<Map<ShoppingCategory, List<ShoppingResponse>>> getItems() {
        return ResponseEntity.ok(shoppingService.getItemsGroupedByCategory());
    }

    // GET items filtered by category
    @GetMapping("/category/{category}")
    public ResponseEntity<List<ShoppingResponse>> getByCategory(
            @PathVariable ShoppingCategory category) {
        return ResponseEntity.ok(shoppingService.getItemsByCategory(category));
    }

    // TOGGLE purchased
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ShoppingResponse> togglePurchased(@PathVariable UUID id) {
        return ResponseEntity.ok(shoppingService.togglePurchased(id));
    }

    // UPDATE item
    @PutMapping("/{id}")
    public ResponseEntity<ShoppingResponse> updateItem(
            @PathVariable UUID id,
            @Valid @RequestBody ShoppingRequest request) {
        return ResponseEntity.ok(shoppingService.updateItem(id, request));
    }

    // DELETE single item
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        shoppingService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }

    // DELETE all purchased items
    @DeleteMapping("/purchased")
    public ResponseEntity<Void> clearPurchased() {
        shoppingService.clearPurchased();
        return ResponseEntity.noContent().build();
    }
}

