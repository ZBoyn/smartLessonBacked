package com.neu.smartLesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentAssessmentDetailDto {
    private Integer assessmentId;
    private String title;
    private String status;
    private String startTime;
    private String endTime;
    private List<QuestionFullDto> questions; // Includes options
}

