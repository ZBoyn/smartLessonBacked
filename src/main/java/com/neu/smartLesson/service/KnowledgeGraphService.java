package com.neu.smartLesson.service;

import com.neu.smartLesson.dto.CreateRelationRequestDto;
import com.neu.smartLesson.dto.GraphDto;
import com.neu.smartLesson.dto.KnowledgePointDto;
import com.neu.smartLesson.dto.RelationResponseDto;

public interface KnowledgeGraphService {

    // 节点 (Nodes)
    /**
     * 在课程中创建新知识点 (US-T07)
     * @param dto 知识点DTO
     * @param courseId 课程ID
     * @param teacherId 当前教师ID (用于鉴权)
     * @return 创建的知识点
     */
    KnowledgePointDto createKnowledgePoint(KnowledgePointDto dto, Integer courseId, Integer teacherId);

    /**
     * 更新知识点 (US-T10)
     * @param kpId 要更新的知识点ID
     * @param dto 包含新数据的DTO
     * @param teacherId 当前教师ID (用于鉴权)
     * @return 更新后的知识点
     */
    KnowledgePointDto updateKnowledgePoint(Integer kpId, KnowledgePointDto dto, Integer teacherId);

    /**
     * 删除知识点 (US-T10)
     * @param kpId 要删除的知识点ID
     * @param teacherId 当前教师ID (用于鉴权)
     */
    void deleteKnowledgePoint(Integer kpId, Integer teacherId);

    // 边 (Edges)
    /**
     * 在课程中创建新关系 (US-T08)
     */
    RelationResponseDto createRelation(CreateRelationRequestDto dto, Integer courseId, Integer teacherId);

    /**
     * 删除关系 (US-T10)
     */
    void deleteRelation(Integer relationId, Integer teacherId);

    // 图 (Graph)

    /**
     * 【新增】获取课程的完整图谱 (US-T09)
     */
    GraphDto getGraphForCourse(Integer courseId, Integer teacherId);
}