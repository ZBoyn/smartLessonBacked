package com.neu.smartLesson.mapper;

import com.neu.smartLesson.model.AIFeedback;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.Optional;
import java.util.List;

@Mapper
public interface AIFeedbackMapper {
    void insert(AIFeedback feedback);
    
    Optional<AIFeedback> findByAnswerId(@Param("answerId") Integer answerId);
    
    void updateTeacherRevision(AIFeedback feedback);

    List<String> findDimensionsByAssessmentId(@Param("assessmentId") Integer assessmentId);
}

