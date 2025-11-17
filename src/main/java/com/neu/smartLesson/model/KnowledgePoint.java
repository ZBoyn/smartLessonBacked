package com.neu.smartLesson.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 对应数据库 'KnowledgePoints' 表的 POJO
 * 这是图谱的“节点”
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgePoint {

    private Integer kpId;

    // 外键, 关联 Courses.course_id
    private Integer courseId;

    private String name;
    private String description;
}