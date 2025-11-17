package com.neu.smartLesson.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthFilter jwtAuthFilter;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // 禁用 CSRF (因为我们使用 JWT, API 是无状态的)
                .csrf(AbstractHttpConfigurer::disable)

                // 定义 API 路径的访问权限
                .authorizeHttpRequests(auth -> auth
                        // 公开白名单: 登录注册接口, API文档(Swagger)
                        .requestMatchers(
                                "/auth/login",
                                "/auth/register",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html"
                        ).permitAll()

                        // 角色权限: 教师端 API
                        .requestMatchers("/teacher/**").hasRole("teacher")
                        .requestMatchers("/student/**").hasRole("student")

                        // 其他所有请求都必须经过认证
                        .anyRequest().authenticated()
                )

                // 设置 Session 管理为无状态 (STATELESS)
                // Spring Security 不会创建或使用 HttpSession
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 注册的 AuthenticationProvider
                .authenticationProvider(authenticationProvider)

                // 将 JWT 过滤器添加到 Spring Security 过滤器链中
                // 放在 UsernamePasswordAuthenticationFilter 之前
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}