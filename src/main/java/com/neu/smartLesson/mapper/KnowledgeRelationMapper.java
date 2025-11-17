package com.neu.smartLesson.mapper;

import com.neu.smartLesson.model.KnowledgeRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 知识点关系 (KnowledgeRelations) 表的 MyBatis Mapper 接口
 */
@Mapper
public interface KnowledgeRelationMapper {

    /**
     * 插入一条新关系 (边)
     * (US-T08)
     */
    void insertRelation(KnowledgeRelation relation);

    /**
     * 根据ID删除一条关系 (边)
     * (US-T10)
     */
    void deleteRelationById(@Param("relationId") Integer relationId);

    /**
     * 根据ID查询一条关系 (用于鉴权)
     */
    Optional<KnowledgeRelation> findRelationById(@Param("relationId") Integer relationId);

    /**
     * 查询一个课程下的所有关系 (用于 US-T09)
     * @param courseId 课程ID
     * @return 边列表
     */
    List<KnowledgeRelation> findRelationsByCourseId(@Param("courseId") Integer courseId);
}