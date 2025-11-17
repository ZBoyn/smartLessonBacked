package com.neu.smartLesson.controller;

import com.neu.smartLesson.dto.ClassResponseDto;
import com.neu.smartLesson.dto.CreateClassRequestDto;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.ClassService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher/courses/{courseId}/classes")
@PreAuthorize("hasRole('TEACHER')")
public class ClassController {

    @Autowired
    private ClassService classService;

    /**
     * (US-T05) 教师为特定课程创建新班级
     * POST /teacher/courses/{courseId}/classes
     */
    @PostMapping
    public ResponseEntity<ClassResponseDto> createClass(
            @PathVariable Integer courseId,
            @Valid @RequestBody CreateClassRequestDto requestDto) {

        // 获取当前登录的教师ID
        Integer teacherId = getCurrentUserId();

        // 调用服务 (注意: 隐式假设了教师有权操作 courseId)
        // (V2.0: 我们应该在这里添加一个检查, 确保 courseId 的 creator_id == teacherId)
        ClassResponseDto createdClass = classService.createClass(requestDto, courseId, teacherId);

        // 返回 201 Created
        return new ResponseEntity<>(createdClass, HttpStatus.CREATED);
    }

    /**
     *  (US-T06) 教师查看特定课程下的所有班级
     * GET /teacher/courses/{courseId}/classes
     */
    @GetMapping
    public ResponseEntity<List<ClassResponseDto>> getClassesForCourse(
            @PathVariable Integer courseId) {

        // (V2.0: 这里也应该检查教师是否有权查看这个 courseId)
        List<ClassResponseDto> classes = classService.getClassesForCourse(courseId);

        return ResponseEntity.ok(classes);
    }

    /**
     * 辅助方法: 从 Spring Security 上下文中获取用户ID
     */
    private Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = (User) authentication.getPrincipal();
        return currentUser.getUserId();
    }
}