package com.neu.smartLesson.controller;

import com.neu.smartLesson.dto.LoginRequest;
import com.neu.smartLesson.dto.LoginResponse;
import com.neu.smartLesson.dto.RegisterRequest;
import com.neu.smartLesson.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    /**
     * POST /auth/login
     * 用户登录接口 (US01, US02)
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        LoginResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    /**
     * POST /auth/register
     * 用户注册接口 (US_Supplement01)
     */
    @PostMapping("/register")
    public ResponseEntity<String> register(@Valid @RequestBody RegisterRequest registerRequest) {

        authService.register(registerRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body("用户注册成功!");
    }
}