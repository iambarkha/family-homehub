package com.aab.homehub.family;

import com.aab.homehub.auth.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FamilyMapper {
    FamilyResponse toResponse(FamilyGroup familyGroup);
    FamilyResponse.FamilyMemberResponse toMemberResponse(User user);
}
