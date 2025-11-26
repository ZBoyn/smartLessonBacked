package com.neu.smartLesson.dto.analysis;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class StudentAnalysisDto {
    private Integer studentId;
    private String name;
    private BigDecimal avgScore; // 平均分
    private Double masteryRate;
    private Double submissionRate;
    private String suggestion;   // 规则建议/AI 提示
}
