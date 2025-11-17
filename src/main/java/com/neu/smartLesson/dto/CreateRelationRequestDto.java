package com.neu.smartLesson.dto;

import com.neu.smartLesson.model.RelationType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 创建知识点关系 (边) 时的请求 DTO (US-T08)
 */
@Data
public class CreateRelationRequestDto {

    @NotNull(message = "起始节点ID (sourceKpId) 不能为空")
    private Integer sourceKpId;

    @NotNull(message = "目标节点ID (targetKpId) 不能为空")
    private Integer targetKpId;

    @NotNull(message = "关系类型 (relationType) 不能为空")
    private RelationType relationType;
}