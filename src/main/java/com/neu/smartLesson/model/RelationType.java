package com.neu.smartLesson.model;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * 知识点关系的类型 (图谱的“边”)
 * 对应数据库 'KnowledgeRelations' 表 'relation_type' 字段
 */
public enum RelationType {
    /** 前置知识 */
    prerequisite,
    /** 属于章节/包含 */
    belongs_to,
    /** 详细解释/相关 */
    explains;

    @JsonCreator
    public static RelationType from(String value) {
        if (value == null) return belongs_to;
        String v = value.trim().toLowerCase();
        if ("prerequisite".equals(v)) return prerequisite;
        if ("belongs_to".equals(v) || "includes".equals(v) || "include".equals(v)) return belongs_to;
        if ("explains".equals(v) || "related".equals(v) || "relation".equals(v)) return explains;
        return belongs_to;
    }
}
