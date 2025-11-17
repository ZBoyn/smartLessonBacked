package com.neu.smartLesson.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface AssessmentQuestionMapper {

    /**
     * 批量插入 测评-题目 关联
     */
    void insertBatchQuestions(
            @Param("assessmentId") Integer assessmentId,
            @Param("questionIds") List<Integer> questionIds
    );

    /**
     * 查询测评包含的题目ID
     */
    List<Integer> findQuestionIdsByAssessmentId(@Param("assessmentId") Integer assessmentId);
}