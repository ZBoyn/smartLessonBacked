package com.neu.smartLesson.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class ResourceResponseDto {
    private Integer resourceId;
    private Integer courseId;
    private String title;
    private String resourceType;
    private String filePath; // API URL (e.g., /resources/123)
    private Integer uploadedById;

    // 同时返回它所关联的知识点
    private List<Integer> knowledgePointIds;
}