package com.neu.smartLesson.mapper;

import com.neu.smartLesson.model.VideoAnalytics;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface VideoAnalyticsMapper {

    List<VideoAnalytics> findByResourceIds(@Param("resourceIds") List<Integer> resourceIds);

    /**
     * 获取指定班级学生的总观看投入（用于行为分析）
     */
    List<java.util.Map<String, Object>> summarizeEngagementByClass(@Param("classId") Integer classId);
}

