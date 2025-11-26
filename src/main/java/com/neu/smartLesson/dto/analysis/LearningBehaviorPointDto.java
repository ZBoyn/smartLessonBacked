package com.neu.smartLesson.dto.analysis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LearningBehaviorPointDto {
    private Integer studentId;
    private String studentName;
    /**
     * 视频学习投入（可理解为分钟或归一化指标）
     */
    private Double engagementScore;
    private Double finalScore;
    private Double submissionRate;
}

