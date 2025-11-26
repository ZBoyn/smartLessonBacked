package com.neu.smartLesson.mapper;

import com.neu.smartLesson.dto.analysis.KpMasteryDto;
import com.neu.smartLesson.dto.analysis.StudentAnalysisDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AnalyticsMapper {

    /**
     * 【核心分析 SQL】
     * 计算某次测评中，班级在各个知识点上的掌握情况
     */
    List<KpMasteryDto> analyzeClassMasteryByAssessment(@Param("assessmentId") Integer assessmentId);

    /**
     * 计算班级整体在各个知识点上的掌握情况 (聚合所有已发布的测评)
     */
    List<KpMasteryDto> analyzeClassOverallMastery(@Param("classId") Integer classId);

    /**
     * 获取班级学生综合分析列表
     */
    List<StudentAnalysisDto> analyzeStudentOverall(@Param("classId") Integer classId);

    /**
     * 获取每个学生在知识点层面的掌握度
     */
    List<com.neu.smartLesson.dto.analysis.StudentKpMasteryDto> analyzeStudentKpMastery(@Param("classId") Integer classId);
}
