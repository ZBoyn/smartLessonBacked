package com.neu.smartLesson.mapper;

import com.neu.smartLesson.model.Assessment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional; // 导入

@Mapper
public interface AssessmentMapper {

    List<com.neu.smartLesson.dto.AssessmentDto> findAssessmentsForStudent(@Param("studentId") Integer studentId);

    /**
     * 插入测评 (草稿)
     */
    void insertAssessment(Assessment assessment);

    /**
     * 根据ID查找测评
     */
    Optional<Assessment> findAssessmentById(@Param("assessmentId") Integer assessmentId);

    /**
     * 更新测评状态 (发布)
     */
    int updateAssessmentStatus(Assessment assessment);
}