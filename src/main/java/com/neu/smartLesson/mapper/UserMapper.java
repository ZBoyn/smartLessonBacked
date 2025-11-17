package com.neu.smartLesson.mapper;

import com.neu.smartLesson.model.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Optional;

@Mapper
public interface UserMapper {
    Optional<User> findByUsername(@Param("username") String username);
    Optional<User> findByEmail(@Param("email") String email);
    void insertUser(User user);
}
