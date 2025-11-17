package com.neu.smartLesson.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface EnrollmentMapper {

    /**
     * 检查一个学生是否注册了某个课程下的任何班级
     *
     * @param studentId 学生ID
     * @param courseId 课程ID
     * @return 注册记录的数量 (0 或 1+)
     */
    Integer checkEnrollment(@Param("studentId") Integer studentId, @Param("courseId") Integer courseId);
}