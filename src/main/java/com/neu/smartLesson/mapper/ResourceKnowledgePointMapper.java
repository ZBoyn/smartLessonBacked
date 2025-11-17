package com.neu.smartLesson.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ResourceKnowledgePointMapper {

    /**
     * 批量插入 资源-知识点 关联
     * @param resourceId 新生成的资源ID
     * @param knowledgePointIds 要关联的知识点ID列表
     */
    void insertBatch(
            @Param("resourceId") Integer resourceId,
            @Param("knowledgePointIds") List<Integer> knowledgePointIds
    );
}