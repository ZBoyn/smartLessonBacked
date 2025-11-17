package com.neu.smartLesson.dto;

import lombok.Builder;
import lombok.Data;
import java.util.List;

/**
 * 知识图谱的统一 DTO (US-T09)
 * 包含节点列表和边列表，用于前端可视化
 */
@Data
@Builder
public class GraphDto {
    private List<KnowledgePointDto> nodes;
    private List<RelationResponseDto> edges;
}