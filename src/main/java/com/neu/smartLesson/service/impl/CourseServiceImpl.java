package com.neu.smartLesson.service.impl;

import com.neu.smartLesson.dto.CourseResponseDto;
import com.neu.smartLesson.dto.CreateCourseRequestDto;
import com.neu.smartLesson.mapper.CourseDtoMapper;
import com.neu.smartLesson.mapper.CourseMapper;
import com.neu.smartLesson.model.Course;
import com.neu.smartLesson.service.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

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
}