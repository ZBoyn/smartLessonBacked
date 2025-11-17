package com.neu.smartLesson.model;

/**
 * 知识点关系的类型 (图谱的“边”)
 * 对应数据库 'KnowledgeRelations' 表 'relation_type' 字段
 */
public enum RelationType {
    /**
     * 前置知识
     */
    prerequisite,

    /**
     * 属于章节
     */
    belongs_to,

    /**
     * 详细解释
     */
    explains
}