package com.neu.smartLesson.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class StudentSubmissionSummaryDto {
    private Integer submissionId;
    private Integer studentId;
    private String studentName; // 联表查询获取
    private String status;      // 'submitted', 'graded'
    private Timestamp submittedAt;
    private BigDecimal totalScore;
}