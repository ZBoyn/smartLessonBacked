package com.neu.smartLesson.service.impl;

import com.neu.smartLesson.mapper.UserMapper;
import com.neu.smartLesson.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Spring Security 用于加载用户数据的服务。
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // ======================= DEBUGGING START =======================
        System.out.println("\n--- [DEBUG] ---");
        System.out.println("UserDetailsServiceImpl: 正在尝试加载用户: " + username);
        // ======================= DEBUGGING END =========================

        User user;
        try {
            System.out.println("--- [DEBUG] ---");
            System.out.println("UserDetailsServiceImpl: 即将调用 userMapper.findByUsername...");
            user = userMapper.findByUsername(username)
                    .orElseThrow(() -> {
                        // ======================= DEBUGGING START =======================
                        System.out.println("--- [DEBUG] ---");
                        System.out.println("UserDetailsServiceImpl: 数据库中未找到用户 (in orElseThrow): " + username);
                        System.out.println("--- [DEBUG] --- \n");
                        // ======================= DEBUGGING END =========================
                        return new UsernameNotFoundException("用户名或密码错误");
                    });
            System.out.println("--- [DEBUG] ---");
            System.out.println("UserDetailsServiceImpl: userMapper.findByUsername 调用成功。");
        } catch (Exception e) {
            System.out.println("--- [DEBUG] ---");
            System.out.println("UserDetailsServiceImpl: 调用 userMapper.findByUsername 时发生异常: " + e.getMessage());
            e.printStackTrace();
            System.out.println("--- [DEBUG] --- \n");
            throw new UsernameNotFoundException("数据库查询时发生内部错误", e);
        }

        // ======================= DEBUGGING START =======================
        System.out.println("--- [DEBUG] ---");
        System.out.println("UserDetailsServiceImpl: 成功在数据库中找到用户: " + user.getUsername());
        System.out.println("UserDetailsServiceImpl: 数据库中存储的Hash是: " + user.getPassword()); // 这会打印出哈希
        System.out.println("--- [DEBUG] --- \n");
        // ======================= DEBUGGING END =========================
        return user;
    }
}