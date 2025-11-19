package com.neu.smartLesson.service;

import com.neu.smartLesson.dto.GradeAnswerRequestDto;
import com.neu.smartLesson.dto.StudentSubmissionSummaryDto;
import com.neu.smartLesson.dto.analysis.KpMasteryDto;
import com.neu.smartLesson.model.User;
import java.util.List;

public interface GradeService {

    /**
     * 1. 获取提交列表
     */
    List<StudentSubmissionSummaryDto> getSubmissionsForAssessment(Integer assessmentId, User teacher);

    /**
     * 2. 人工打分 (针对某一道题)
     * 打分后自动更新 Submission 的总分
     */
    void gradeAnswer(Integer answerId, GradeAnswerRequestDto dto, User teacher);

    /**
     * 3. 学情分析：知识点掌握度 (Task 1 核心)
     */
    List<KpMasteryDto> analyzeKnowledgeMastery(Integer assessmentId, User teacher);
}