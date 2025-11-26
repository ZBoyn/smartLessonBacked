package com.neu.smartLesson.dto;

import lombok.Data;

@Data
public class AnswerFullDetailsDto {
    private Integer answerId;
    private String answerText;
    private String questionPrompt;
    private String studentId; // String for flexibility, though DB is int
    // Add more fields if needed, e.g. reference answer
}

