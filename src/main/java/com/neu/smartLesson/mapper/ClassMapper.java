package com.neu.smartLesson.mapper;

import com.neu.smartLesson.model.CourseClass;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

@Mapper
public interface ClassMapper {

    /**
     * 插入一个新班级
     * (US-T05)
     * @param courseClass CourseClass POJO (必须包含 courseId, className, teacherId)
     */
    void insertClass(CourseClass courseClass);

    /**
     * 根据课程ID查询该课程下的所有班级
     * (US-T06)
     * @param courseId 课程 (Course) 的 ID
     * @return 该课程的班级列表
     */
    List<CourseClass> findClassesByCourseId(@Param("courseId") Integer courseId);

    /**
     * 根据ID查询班级 (用于鉴权)
     * @param classId 班级 ID
     * @return 班级实体，找不到时返回空 Optional
     */
    Optional<CourseClass> findClassById(@Param("classId") Integer classId);
}