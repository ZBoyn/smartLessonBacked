package com.neu.smartLesson.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIFeedback {
    private Integer feedbackId;
    private Integer answerId;
    private BigDecimal aiGeneratedScore;
    private String aiGeneratedSummary;
    private String aiGeneratedSuggestions;
    private String dimensions; // JSON
    private BigDecimal teacherRevisedScore;
    private String teacherRevisedFeedback;
    private Boolean isRevisedByTeacher;
}

