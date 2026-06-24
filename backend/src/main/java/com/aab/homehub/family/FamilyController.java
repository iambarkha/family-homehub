package com.aab.homehub.family;

import com.aab.homehub.family.dto.FamilyRequest;
import com.aab.homehub.family.dto.FamilyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/family")
public class FamilyController {

    private FamilyService familyService;

    public FamilyController(FamilyService familyService) {
        this.familyService = familyService;
    }

    /*@PostMapping("/createFamily/{familyName}/users/{userId}")
    public ResponseEntity<FamilyResponse> createFamily(@PathVariable String familyName, @PathVariable String userId) {
*/
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FamilyResponse> createFamily(@Valid @RequestBody FamilyRequest familyRequest) {

        FamilyResponse familyResponse = familyService.createFamily(familyRequest.name(),familyRequest.creatorUserId());

        return ResponseEntity.status(HttpStatus.CREATED).body(familyResponse);
    }
    @DeleteMapping("/families/{familyId}/users/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> removeFamily(@PathVariable String familyId, @PathVariable String userId) {

        boolean deleted = familyService.removeFamily(familyId, userId);

        if (deleted) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }
    @GetMapping
    public ResponseEntity<FamilyResponse> getFamilyById(@RequestParam String id) {
        return  ResponseEntity.ok(familyService.getFamilyById(id));
    }

    @PostMapping("/addMember/families/{familyId}/email/{email}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FamilyResponse> addMemberByEmail(@PathVariable String familyId, @PathVariable String email) {
        FamilyResponse familyResponse = familyService.addMemberByEmail(familyId, email);
        return ResponseEntity.ok(familyResponse);
    }


}
