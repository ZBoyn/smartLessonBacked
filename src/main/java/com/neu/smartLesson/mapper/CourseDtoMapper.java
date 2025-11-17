package com.neu.smartLesson.mapper;

import com.neu.smartLesson.dto.CourseResponseDto;
import com.neu.smartLesson.model.Course;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CourseDtoMapper {

    /**
     * 将 Course (Model) 转换为 CourseResponseDto (DTO)
     */
    public CourseResponseDto toCourseResponseDto(Course course) {
        if (course == null) {
            return null;
        }
        return CourseResponseDto.builder()
                .courseId(course.getCourseId())
                .courseName(course.getCourseName())
                .description(course.getDescription())
                .creatorId(course.getCreatorId())
                .build();
    }

    /**
     * 转换列表
     */
    public List<CourseResponseDto> toCourseResponseDtoList(List<Course> courses) {
        return courses.stream()
                .map(this::toCourseResponseDto)
                .collect(Collectors.toList());
    }
}