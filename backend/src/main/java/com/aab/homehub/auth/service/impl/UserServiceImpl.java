package com.aab.homehub.auth.service.impl;


import com.aab.homehub.auth.UserMapper;
import com.aab.homehub.auth.UserRepository;
import com.aab.homehub.auth.dto.response.UserResponse;
import com.aab.homehub.auth.entity.User;
import com.aab.homehub.auth.service.UserService;
import com.aab.homehub.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserResponse getUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        return userMapper.toUserResponse(user);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public UserResponse getUserByEmail(String email) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!currentUserEmail.equals(email)) {
            throw new AccessDeniedException("You are not authorized to access this user's data");
        }
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse updateUser(UUID userId, UserResponse userResponse) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        user.setFirstName(userResponse.firstName());
        user.setLastName(userResponse.lastName());
        user.setPhoneNumber(userResponse.phoneNumber());

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }

    @Override
    public void deleteUser(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        userRepository.delete(user);
    }

    @Override
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toUserResponse)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponse getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        return userMapper.toUserResponse(user);
    }

    @Override
    public UserResponse updateCurrentUser(UserResponse userResponse) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        user.setFirstName(userResponse.firstName());
        user.setLastName(userResponse.lastName());
        user.setPhoneNumber(userResponse.phoneNumber());

        User updatedUser = userRepository.save(user);
        return userMapper.toUserResponse(updatedUser);
    }


}
