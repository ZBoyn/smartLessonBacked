package com.neu.smartLesson.dto;

import lombok.Builder;
import lombok.Data;
import java.sql.Timestamp;

@Data
@Builder
public class SubmissionResponseDto {
    private Integer submissionId;
    private Integer assessmentId;
    private String status;
    private Timestamp submittedAt;
}