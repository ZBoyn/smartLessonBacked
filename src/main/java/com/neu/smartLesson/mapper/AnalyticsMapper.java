package com.neu.smartLesson.mapper;

import com.neu.smartLesson.dto.analysis.KpMasteryDto;
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
}