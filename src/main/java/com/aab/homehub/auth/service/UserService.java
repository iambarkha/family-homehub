package com.aab.homehub.auth.service;


import com.aab.homehub.auth.dto.response.UserResponse;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse getUserById(UUID userId);

    UserResponse getUserByEmail(String email);

    UserResponse updateUser(UUID userId, UserResponse userResponse);

    void deleteUser(UUID userId);

    boolean existsByEmail(String email);

    // Additional methods used by controller
    List<UserResponse> getAllUsers();

    UserResponse getCurrentUser();

    UserResponse updateCurrentUser(UserResponse userResponse);
}
