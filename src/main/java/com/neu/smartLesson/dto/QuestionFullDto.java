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
public class QuestionFullDto {
    private Integer questionId;
    private String prompt;
    private String questionType;
    private String difficulty;
    private Boolean supportsAiGrading;
    private List<QuestionOptionDto> options;
    private List<Integer> knowledgePointIds;
}
