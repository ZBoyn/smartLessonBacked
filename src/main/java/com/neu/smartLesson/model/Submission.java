package com.neu.smartLesson.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Submission {
    private Integer submissionId;
    private Integer assessmentId;
    private Integer studentId;

    // 'in_progress', 'submitted', 'graded'
    private String status;

    private Timestamp submittedAt;
    private BigDecimal totalScore;
    private Boolean isPublishedToStudent;
}