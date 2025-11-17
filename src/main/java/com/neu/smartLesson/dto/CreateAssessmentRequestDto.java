package com.neu.smartLesson.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class CreateAssessmentRequestDto {
    @NotBlank(message = "测评标题不能为空")
    private String title;

    @NotBlank(message = "测评类型不能为空")
    private String assessmentType; // e.g., 'homework_report'

    @NotEmpty(message = "必须至少选择一个题目")
    private List<Integer> questionIds;

    // V2.0: 我们还应该传递每道题的分值
}