package com.neu.smartLesson.mapper;

import com.neu.smartLesson.dto.AuthUserDto;
import com.neu.smartLesson.model.User;
import org.springframework.stereotype.Component;

@Component
public class AuthDtoMapper {

    /**
     * 将 User 实体 转换为 AuthUserDto
     * @param user User 实体
     * @return AuthUserDto
     */
    public AuthUserDto toAuthUserDto(User user) {
        if (user == null) {
            return null;
        }

        return AuthUserDto.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }
}