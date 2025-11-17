package com.neu.smartLesson.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.sql.Timestamp;

/**
 * 对应数据库 'Assessments' 表的 POJO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Assessment {
    private Integer assessmentId;
    private Integer classId;
    private String title;
    private String assessmentType; // e.g., 'exam', 'homework_report'
    private String status;         // e.g., 'draft', 'published', 'closed'
    private Timestamp startTime;
    private Timestamp endTime;
    private Integer createdById;
}