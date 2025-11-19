package com.neu.smartLesson.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class GradeAnswerRequestDto {
    @NotNull
    private BigDecimal score; // 教师给的分数

    private String feedback;  // 教师评语
}