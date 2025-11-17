package com.neu.smartLesson.dto;

import lombok.Builder;
import lombok.Data;
import java.sql.Timestamp;

/**
 * 学生查看作业列表时返回的 DTO (US23)
 */
@Data
@Builder
public class AssessmentDto {

    private Integer assessmentId;
    private String title;
    private String assessmentType;
    private String status;
    private Timestamp endTime;

    // 我们可以添加一个字段，告诉学生他们是否已提交
    private String submissionStatus;
}