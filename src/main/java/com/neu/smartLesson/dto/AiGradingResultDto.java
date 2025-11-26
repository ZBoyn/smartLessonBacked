package com.neu.smartLesson.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiGradingResultDto {
    private Double totalScore;
    private String summary;
    private String suggestions; // JSON string or joined string
    private String dimensions; // JSON string
}

