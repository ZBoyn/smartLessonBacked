package com.neu.smartLesson.dto;

import com.neu.smartLesson.model.RelationType;
import lombok.Builder;
import lombok.Data;

/**
 * 返回知识点关系 (边) 时的响应 DTO
 */
@Data
@Builder
public class RelationResponseDto {
    private Integer relationId;
    private Integer sourceKpId;
    private Integer targetKpId;
    private RelationType relationType;
}