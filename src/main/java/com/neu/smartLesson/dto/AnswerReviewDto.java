package com.neu.smartLesson.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AnswerReviewDto {
    private Integer answerId;
    private Integer questionId;
    private String questionPrompt;
    private String questionType;
    private Boolean supportsAiGrading;
    private Double maxPoints;
    private String answerText;
    private String filePath;
    private BigDecimal score;
    private String feedback;
}

