package com.neu.smartLesson.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.sql.Timestamp;

@Data
public class PublishAssessmentRequestDto {
    @NotNull(message = "开始时间不能为空")
    private Timestamp startTime;

    @NotNull(message = "结束时间不能为空")
    @Future(message = "结束时间必须在未来")
    private Timestamp endTime;
}