package com.neu.smartLesson.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * 对应数据库 'KnowledgeRelations' 表的 POJO
 * 这是图谱的“边”
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeRelation {

    private Integer relationId;

    private Integer sourceKpId;

    private Integer targetKpId;

    private RelationType relationType;
}