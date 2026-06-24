package com.aab.homehub.family;

import com.aab.homehub.auth.UserRepository;
import com.aab.homehub.auth.entity.User;
import com.aab.homehub.exception.ResourceNotFoundException;
import com.aab.homehub.family.dto.FamilyResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class FamilyService {

    private final FamilyRepository familyRepository;
    private final FamilyMapper familyMapper;
    private final UserRepository userRepository;

    public FamilyResponse createFamily(String familyName, String creatorUserId) {
        if (familyName == null || familyName.isBlank() || creatorUserId == null || creatorUserId.isBlank()) {
            throw new IllegalArgumentException("Family name and creator user id are required");
        }
        // Check if the user already belongs to a family
        User creator = userRepository.findById(UUID.fromString(creatorUserId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + creatorUserId));

        if (creator.getFamilyGroup() != null) {
            throw new IllegalStateException("User already belongs to a family");
        }

        FamilyGroup familyGroup = FamilyGroup.builder()
                .name(familyName)
                .build();

        FamilyGroup savedFamily = familyRepository.save(familyGroup);

        creator.setFamilyGroup(savedFamily);
        userRepository.save(creator);

        return familyMapper.toResponse(savedFamily);

    }

    @Transactional(readOnly = true)
    public FamilyResponse getFamilyById(String familyId) {
        FamilyGroup familyGroup = familyRepository.findById(UUID.fromString(familyId))
                .orElseThrow(() -> new ResourceNotFoundException("Family with id: " + familyId));
        return familyMapper.toResponse(familyGroup);
    }

    public FamilyResponse addMemberByEmail(String familyId, String email) {
        FamilyGroup familyGroup = familyRepository.findById(UUID.fromString(familyId))
                .orElseThrow(() -> new ResourceNotFoundException("Family not found with id: " + familyId));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setFamilyGroup(familyGroup);
        userRepository.save(user);

        return familyMapper.toResponse(familyGroup);
    }

    @Transactional
     public boolean removeFamily(String familyId, String userId) {
        FamilyGroup familyGroup = familyRepository.findById(UUID.fromString(familyId))
                .orElseThrow(() -> new ResourceNotFoundException("Family not found with id : " + familyId));

        User user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id : " + userId));

        if (user.getFamilyGroup() == null ||
                !user.getFamilyGroup().getId().equals(familyGroup.getId())) {
            throw new ResourceNotFoundException("User with id: " + userId + "is not a member of this family");
        }

        user.setFamilyGroup(null);
        userRepository.save(user);

        return true;
    }
}
