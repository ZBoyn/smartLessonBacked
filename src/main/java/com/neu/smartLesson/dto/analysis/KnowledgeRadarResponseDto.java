package com.neu.smartLesson.dto.analysis;

import lombok.Data;

import java.util.List;

@Data
public class KnowledgeRadarResponseDto {
    private List<RadarIndicatorDto> indicators;
    /**
     * 班级平均掌握度数组，对应 indicators 顺序
     */
    private List<Double> classAvg;
    /**
     * 班级最佳学生掌握度
     */
    private List<Double> topStudent;
    /**
     * 及格率/通过率
     */
    private List<Double> passRate;
}

