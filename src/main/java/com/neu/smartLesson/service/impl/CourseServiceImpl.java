package com.neu.smartLesson.service.impl;

import com.neu.smartLesson.dto.CourseResponseDto;
import com.neu.smartLesson.dto.CreateCourseRequestDto;
import com.neu.smartLesson.exception.ResourceNotFoundException;
import com.neu.smartLesson.exception.UnauthorizedException;
import com.neu.smartLesson.mapper.CourseDtoMapper;
import com.neu.smartLesson.mapper.CourseMapper;
import com.neu.smartLesson.model.Course;
import com.neu.smartLesson.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Objects;

@Service
public class CourseServiceImpl implements CourseService {

    @Autowired
    private CourseMapper courseMapper;

    @Autowired
    private CourseDtoMapper courseDtoMapper;

    @Override
    public CourseResponseDto createCourse(CreateCourseRequestDto requestDto, Integer creatorId) {

        Course newCourse = Course.builder()
                .courseName(requestDto.getCourseName())
                .description(requestDto.getDescription())
                .creatorId(creatorId)
                .build();

        courseMapper.insertCourse(newCourse);

        return courseDtoMapper.toCourseResponseDto(newCourse);
    }

    @Override
    public List<CourseResponseDto> getCoursesForTeacher(Integer creatorId) {

        List<Course> courses = courseMapper.findCoursesByCreatorId(creatorId);

        return courseDtoMapper.toCourseResponseDtoList(courses);
    }

    /**
     * 实现鉴权逻辑
     */
    @Override
    public Course checkCourseOwnership(Integer courseId, Integer teacherId) {
        Course course = courseMapper.findCourseById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException("课程 (Course) 未找到, ID: " + courseId));

        if (!Objects.equals(course.getCreatorId(), teacherId)) {
            throw new UnauthorizedException(
                    "您 (ID: " + teacherId + ") 无权操作此课程 (ID: " + courseId + ")"
            );
        }
        return course;
    }

    @Override
    public CourseResponseDto getCourseDetails(Integer courseId, Integer teacherId) {
        Course course = checkCourseOwnership(courseId, teacherId);
        return courseDtoMapper.toCourseResponseDto(course);
    }
}
