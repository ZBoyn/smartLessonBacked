package com.neu.smartLesson.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResourceKnowledgePoint {
    private Integer id;
    private Integer resourceId;
    private Integer kpId;
}