package com.neu.smartLesson.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface QuestionKnowledgePointMapper {
    void insertBatchKPs(
            @Param("questionId") Integer questionId,
            @Param("knowledgePointIds") List<Integer> knowledgePointIds
    );
    void deleteKPsByQuestionId(@Param("questionId") Integer questionId);
    List<Integer> findKPsByQuestionId(@Param("questionId") Integer questionId);

    java.util.List<java.util.Map<String, Object>> countQuestionsByKpForCourse(@Param("courseId") Integer courseId);
}
