package com.aab.homehub.auth;

import com.aab.homehub.auth.dto.request.RegisterRequest;
import com.aab.homehub.auth.dto.response.UserResponse;
import com.aab.homehub.auth.entity.User;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface UserMapper {

    UserResponse toUserResponse(User user);

    User toUser(RegisterRequest registerRequest);
}

