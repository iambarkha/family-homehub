package com.aab.homehub.auth.service.impl;

import com.aab.homehub.auth.dto.request.LoginRequest;
import com.aab.homehub.auth.dto.request.RegisterRequest;
import com.aab.homehub.auth.dto.response.ApiResponse;
import com.aab.homehub.auth.dto.response.AuthResponse;
import com.aab.homehub.auth.dto.response.UserResponse;
import com.aab.homehub.auth.entity.User;
import com.aab.homehub.auth.entity.Role;
import com.aab.homehub.auth.UserMapper;
import com.aab.homehub.auth.UserRepository;
import com.aab.homehub.auth.service.AuthService;
import com.aab.homehub.auth.service.JwtService;
import com.aab.homehub.exception.InvalidCredentialsException;
import com.aab.homehub.exception.ResourceNotFoundException;
import com.aab.homehub.exception.UserAlreadyExistsException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    @Override
    public ApiResponse<AuthResponse> register(RegisterRequest registerRequest) {
        if(userRepository.existsByEmail(registerRequest.email())) {
            throw new UserAlreadyExistsException(("Email already registered: " + registerRequest.email()));
        }
        User user = userMapper.toUser(registerRequest);
        user.setRole(Role.BASIC);
        user.setPassword(passwordEncoder.encode(registerRequest.password()));

        User savedUser = userRepository.save(user);

       AuthResponse response =  new AuthResponse( jwtService.generateToken(savedUser),
               "Bearer", 86400L, userMapper.toUserResponse(savedUser));

        return ApiResponse.success("User registered successfully", response);
    }

    @Override
    public ApiResponse<AuthResponse> login(LoginRequest loginRequest) {
        User user = userRepository.findByEmail(loginRequest.email())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        AuthResponse authResponse = new AuthResponse(
                jwtService.generateToken(user),
                "Bearer",
                86400L,
                userMapper.toUserResponse(user)
        );
        return ApiResponse.success("User registered successfully", authResponse);

    }

    @Override
    public UserResponse getUserProfile(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse updateUserProfile(UUID userId, UserResponse userResponse) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(userResponse.firstName());
        user.setLastName(userResponse.lastName());
        user.setPhoneNumber(userResponse.phoneNumber());

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with userid: " + userId));
        userRepository.delete(user);
    }
}
