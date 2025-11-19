package com.neu.smartLesson.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Answer {
    private Integer answerId;
    private Integer submissionId;
    private Integer questionId;

    // 简答题内容
    private String answerText;

    // 报告题文件路径 (Task 3 核心)
    private String filePath;

    private BigDecimal score;
    private String feedback; // 老师或AI的评语
}