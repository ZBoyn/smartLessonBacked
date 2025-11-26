package com.neu.smartLesson.service;

import com.neu.smartLesson.dto.AiGradingResultDto;

public interface AiGradingService {
    AiGradingResultDto gradeSingleAnswer(String studentId, String questionContent, String studentAnswer, 
                                         String referenceAnswer, Integer maxScore);
}

