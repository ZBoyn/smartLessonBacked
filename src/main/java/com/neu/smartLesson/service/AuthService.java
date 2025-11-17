package com.neu.smartLesson.service;

import com.neu.smartLesson.dto.LoginRequest;
import com.neu.smartLesson.dto.LoginResponse;
import com.neu.smartLesson.dto.RegisterRequest;

public interface AuthService {

    /**
     * 处理用户登录认证
     * @param loginRequest 登录请求 DTO
     * @return 登录响应 DTO (包含 Token)
     */
    LoginResponse login(LoginRequest loginRequest);

    void register(RegisterRequest registerRequest);
}