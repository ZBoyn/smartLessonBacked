package com.neu.smartLesson.mapper;

import com.neu.smartLesson.model.Course;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface CourseMapper {

    /**
     * 插入一个新课程
     * (US-T01)
     * @param course Course POJO (必须包含 courseName, description, creatorId)
     */
    void insertCourse(Course course);

    /**
     * 根据教师ID查询其创建的所有课程
     * (US-T02)
     * @param creatorId 教师 (User) 的 ID
     * @return 该教师的课程列表
     */
    List<Course> findCoursesByCreatorId(@Param("creatorId") Integer creatorId);

    /**
     * 根据ID查询课程 (用于鉴权)
     */
    Optional<Course> findCourseById(@Param("courseId") Integer courseId);
}