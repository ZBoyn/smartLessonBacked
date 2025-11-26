package com.neu.smartLesson.controller;

import com.neu.smartLesson.dto.CourseResponseDto;
import com.neu.smartLesson.dto.CreateCourseRequestDto;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.CourseService;
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
@RequestMapping("/teacher/courses")
@PreAuthorize("hasRole('TEACHER')")
public class CourseController {

    @Autowired
    private CourseService courseService;
    @Autowired
    private com.neu.smartLesson.mapper.ClassMapper classMapper;
    @Autowired
    private com.neu.smartLesson.mapper.EnrollmentMapper enrollmentMapper;

    /**
     * (US-T01) 教师创建新课程
     * POST /teacher/courses
     */
    @PostMapping
    public ResponseEntity<CourseResponseDto> createCourse(
            @Valid @RequestBody CreateCourseRequestDto requestDto) {

        Integer creatorId = getCurrentUserId();

        CourseResponseDto createdCourse = courseService.createCourse(requestDto, creatorId);

        return new ResponseEntity<>(createdCourse, HttpStatus.CREATED);
    }

    /**
     * (US-T02) 教师查看自己创建的课程列表
     * GET /teacher/courses
     */
    @GetMapping
    public ResponseEntity<List<CourseResponseDto>> getMyCourses() {

        Integer teacherId = getCurrentUserId();

        List<CourseResponseDto> courses = courseService.getCoursesForTeacher(teacherId);

        return ResponseEntity.ok(courses);
    }

    /**
     * 获取单个课程详情
     * GET /teacher/courses/{courseId}
     */
    @GetMapping("/{courseId}")
    public ResponseEntity<CourseResponseDto> getCourseDetails(@PathVariable Integer courseId) {
        Integer teacherId = getCurrentUserId();
        CourseResponseDto course = courseService.getCourseDetails(courseId, teacherId);
        return ResponseEntity.ok(course);
    }

    @GetMapping("/{courseId}/stats")
    public ResponseEntity<java.util.Map<String, Object>> getCourseStats(@PathVariable Integer courseId) {
        Integer teacherId = getCurrentUserId();
        courseService.checkCourseOwnership(courseId, teacherId);
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        java.util.List<com.neu.smartLesson.model.CourseClass> classes = classMapper.findClassesByCourseId(courseId);
        int classCount = classes == null ? 0 : classes.size();
        int studentCount = 0;
        if (classes != null) {
            for (com.neu.smartLesson.model.CourseClass c : classes) {
                Integer cnt = enrollmentMapper.countByClassId(c.getClassId());
                studentCount += (cnt == null ? 0 : cnt);
            }
        }
        stats.put("classCount", classCount);
        stats.put("studentCount", studentCount);
        return ResponseEntity.ok(stats);
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
