package com.aab.homehub.pantry;

import com.aab.homehub.pantry.dto.PantryRequest;
import com.aab.homehub.pantry.dto.PantryResponse;
import com.aab.homehub.pantry.dto.PantryUpdateQuantityRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pantry")
@RequiredArgsConstructor
public class PantryController {

    private final PantryService pantryService;

    // ADD item
    @PostMapping
    public ResponseEntity<PantryResponse> addItem(
            @Valid @RequestBody PantryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pantryService.addItem(request));
    }

    // GET all items
    @GetMapping
    public ResponseEntity<List<PantryResponse>> getAllItems() {
        return ResponseEntity.ok(pantryService.getAllItems());
    }

    // GET low stock items only
    @GetMapping("/low-stock")
    public ResponseEntity<List<PantryResponse>> getLowStockItems() {
        return ResponseEntity.ok(pantryService.getLowStockItems());
    }

    // UPDATE quantity — restock or consume
    @PatchMapping("/{id}/quantity")
    public ResponseEntity<PantryResponse> updateQuantity(
            @PathVariable UUID id,
            @Valid @RequestBody PantryUpdateQuantityRequest request) {
        return ResponseEntity.ok(pantryService.updateQuantity(id, request));
    }

    // UPDATE item details
    @PutMapping("/{id}")
    public ResponseEntity<PantryResponse> updateItem(
            @PathVariable UUID id,
            @Valid @RequestBody PantryRequest request) {
        return ResponseEntity.ok(pantryService.updateItem(id, request));
    }

    // DELETE item
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable UUID id) {
        pantryService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
