package com.neu.smartLesson.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionOptionDto {
    private Integer optionId; // 响应时填充

    @NotBlank(message = "选项内容不能为空")
    private String optionText;

    private Boolean isCorrect;
}
