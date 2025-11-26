package com.neu.smartLesson.service;

import com.neu.smartLesson.dto.GradeAnswerRequestDto;
import com.neu.smartLesson.dto.StudentSubmissionSummaryDto;
import com.neu.smartLesson.dto.analysis.KpMasteryDto;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.dto.AiGradingResultDto;
import java.util.List;
import java.util.Map;

public interface GradeService {

    /**
     * AI 自动批改单题
     */
    AiGradingResultDto aiGradeAnswer(Integer answerId, User teacher);

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

    /**
     * 4. 班级整体学情分析 (Radar Chart)
     */
    List<KpMasteryDto> analyzeClassOverall(Integer classId, User teacher);

    /**
     * 5. 班级学生综合画像
     */
    List<com.neu.smartLesson.dto.analysis.StudentAnalysisDto> analyzeStudentOverall(Integer classId, User teacher);

    List<Map<String, Object>> summarizeAiFeedback(Integer assessmentId, User teacher);
}
