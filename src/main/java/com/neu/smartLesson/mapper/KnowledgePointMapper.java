package com.neu.smartLesson.mapper;

import com.neu.smartLesson.model.KnowledgePoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Optional;

/**
 * 知识点 (KnowledgePoints) 表的 MyBatis Mapper 接口
 */
@Mapper
public interface KnowledgePointMapper {

    /**
     * 插入一个新知识点 (节点)
     * (US-T07)
     * @param kp KnowledgePoint POJO
     */
    void insertKnowledgePoint(KnowledgePoint kp);

    /**
     * 更新一个知识点 (US-T10)
     * @param kp KnowledgePoint POJO
     * @return 影响的行数 (用于确认更新是否成功)
     */
    int updateKnowledgePoint(KnowledgePoint kp);

    /**
     * 根据ID删除一个知识点 (US-T10)
     * @param kpId 知识点ID
     */
    void deleteKnowledgePointById(@Param("kpId") Integer kpId);

    /**
     * 根据ID查询一个知识点
     * @param kpId 知识点ID
     * @return 知识点
     */
    Optional<KnowledgePoint> findKnowledgePointById(@Param("kpId") Integer kpId);

    /**
     * 查询一个课程下的所有知识点 (用于 US-T09)
     * @param courseId 课程ID
     * @return 节点列表
     */
    List<KnowledgePoint> findKnowledgePointsByCourseId(@Param("courseId") Integer courseId);
}