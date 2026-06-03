package com.aab.homehub.auth.service;


import com.aab.homehub.auth.dto.request.LoginRequest;
import com.aab.homehub.auth.dto.request.RegisterRequest;
import com.aab.homehub.auth.dto.response.ApiResponse;
import com.aab.homehub.auth.dto.response.AuthResponse;
import com.aab.homehub.auth.dto.response.UserResponse;

import java.util.UUID;

public interface AuthService {

    AuthResponse register(RegisterRequest registerRequest);

    AuthResponse login(LoginRequest loginRequest);

    UserResponse getUserProfile(UUID userId);

    UserResponse updateUserProfile(UUID userId, UserResponse userResponse);

    void deleteUser(UUID userId);
}

