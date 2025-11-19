package com.neu.smartLesson.dto;

import lombok.Data;
import java.util.List;

@Data
public class SubmitAnswersRequestDto {
    // 列表包含多个题目的回答
    private List<SingleAnswerDto> answers;

    @Data
    public static class SingleAnswerDto {
        private Integer questionId;
        private String answerText; // 简答题文本
        private List<Integer> selectedOptionIds; // 选择题选项
    }
}