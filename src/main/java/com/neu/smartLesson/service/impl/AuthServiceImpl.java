package com.neu.smartLesson.service.impl;


import com.neu.smartLesson.dto.AuthUserDto;
import com.neu.smartLesson.dto.LoginRequest;
import com.neu.smartLesson.dto.LoginResponse;
import com.neu.smartLesson.dto.RegisterRequest;
import com.neu.smartLesson.exception.RegistrationException;
import com.neu.smartLesson.mapper.AuthDtoMapper;
import com.neu.smartLesson.mapper.UserMapper;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.AuthService;
import com.neu.smartLesson.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthDtoMapper authMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        loginRequest.getUsername(),
                        loginRequest.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user);
        AuthUserDto userDto = authMapper.toAuthUserDto(user);

        return new LoginResponse(token, userDto);
    }

    @Override
    public void register(RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        userMapper.findByUsername(registerRequest.getUsername())
                .ifPresent(user -> {
                    throw new RegistrationException("错误: 用户名 '" + user.getUsername() + "' 已经存在!");
                });

        // 检查邮箱是否已存在
        userMapper.findByEmail(registerRequest.getEmail())
                .ifPresent(user -> {
                    throw new RegistrationException("错误: 邮箱 '" + user.getEmail() + "' 已经被注册!");
                });

        // 加密密码
        String hashedPassword = passwordEncoder.encode(registerRequest.getPassword());

        // 创建新的 User POJO
        User newUser = User.builder()
                .username(registerRequest.getUsername())
                .passwordHash(hashedPassword)
                .fullName(registerRequest.getFullName())
                .email(registerRequest.getEmail())
                .role(registerRequest.getRole())
                // createdAt 数据库会自动生成
                .build();

        // 5. 插入数据库
        userMapper.insertUser(newUser);

        // 注册成功，无返回值 (void)
    }
}