package com.aab.homehub.family;

import com.aab.homehub.auth.entity.User;

import java.util.List;

public record FamilyResponse (
        String id,
        String name,
        List<FamilyMemberResponse> members
){
    public record FamilyMemberResponse(
            String id,
            String firstName,
            String email
    ) {}
}
