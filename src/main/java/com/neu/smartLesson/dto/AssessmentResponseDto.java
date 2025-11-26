package com.neu.smartLesson.dto;

import lombok.Builder;
import lombok.Data;
import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
public class AssessmentResponseDto {
    private Integer assessmentId;
    private Integer classId;
    private String title;
    private String assessmentType;
    private String status;
    private Timestamp startTime;
    private Timestamp endTime;
    private List<Integer> questionIds;
}