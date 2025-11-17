package com.neu.smartLesson.service;

import com.neu.smartLesson.dto.CourseResponseDto;
import com.neu.smartLesson.dto.CreateCourseRequestDto;
import java.util.List;

public interface CourseService {

    /**
     * 教师创建新课程 (US-T01)
     * @param requestDto 包含课程名称和描述
     * @param creatorId  创建者 (教师) 的 ID
     * @return 创建成功的课程信息
     */
    CourseResponseDto createCourse(CreateCourseRequestDto requestDto, Integer creatorId);

    /**
     * 获取指定教师创建的所有课程 (US-T02)
     * @param creatorId 教师的 ID
     * @return 课程 DTO 列表
     */
    List<CourseResponseDto> getCoursesForTeacher(Integer creatorId);
}