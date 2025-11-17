package com.neu.smartLesson.config;

import com.neu.smartLesson.util.JwtUtil;
import com.neu.smartLesson.service.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // 检查 'Authorization' 头是否存在或是否以 'Bearer ' 开头
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // 如果没有 Token，放行给下一个过滤器
            return;
        }

        // 提取 Token
        final String jwt = authHeader.substring(7); // "Bearer ".length() == 7
        final String username;

        try {
            username = jwtUtil.extractUsername(jwt);
        } catch (Exception e) {
            // Token 解析失败 (例如: 过期, 签名无效)
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token");
            return;
        }


        // 验证 Token
        // 检查用户名不为空，且 SecurityContext 中目前没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

            // 如果 Token 有效 (用户名匹配且未过期)
            if (jwtUtil.validateToken(jwt, userDetails)) {
                // 创建一个 Spring Security 认证凭证
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // 凭证 (密码) 为 null，因为是用 Token 认证
                        userDetails.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                // 将凭证存入 SecurityContext，表示该用户已认证
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 继续执行过滤器链
        filterChain.doFilter(request, response);
    }
}