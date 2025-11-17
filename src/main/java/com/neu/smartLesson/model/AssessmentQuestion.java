package com.neu.smartLesson.model;

import lombok.Builder;
import lombok.Data;

/**
 * 对应数据库 'AssessmentQuestions' 表的 POJO
 * (多对多关联)
 */
@Data
@Builder
public class AssessmentQuestion {
    private Integer id;
    private Integer assessmentId;
    private Integer questionId;
    private Double points;      // 该题分值
    private Integer orderIndex; // 题目顺序
}