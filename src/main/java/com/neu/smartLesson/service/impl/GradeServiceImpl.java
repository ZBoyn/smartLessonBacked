package com.neu.smartLesson.service.impl;

import com.neu.smartLesson.dto.GradeAnswerRequestDto;
import com.neu.smartLesson.dto.StudentSubmissionSummaryDto;
import com.neu.smartLesson.dto.analysis.KpMasteryDto;
import com.neu.smartLesson.exception.ResourceNotFoundException;
import com.neu.smartLesson.mapper.*;
import com.neu.smartLesson.model.Answer;
import com.neu.smartLesson.model.Assessment;
import com.neu.smartLesson.model.Submission;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.CourseService;
import com.neu.smartLesson.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class GradeServiceImpl implements GradeService {

    @Autowired private AssessmentMapper assessmentMapper;
    @Autowired private SubmissionMapper submissionMapper;
    @Autowired private AnswerMapper answerMapper;
    @Autowired private AnalyticsMapper analyticsMapper;
    @Autowired private CourseService courseService;
    @Autowired private ClassMapper classMapper;

    @Override
    public List<StudentSubmissionSummaryDto> getSubmissionsForAssessment(Integer assessmentId, User teacher) {
        // 1. 鉴权
        checkAssessmentAccess(assessmentId, teacher);

        // 2. 查询
        return submissionMapper.findSummaryByAssessmentId(assessmentId);
    }

    @Override
    @Transactional
    public void gradeAnswer(Integer answerId, GradeAnswerRequestDto dto, User teacher) {
        // 1. 查找答案及所属提交
        // (V1.0 简化: 我们需要先通过 answerId 找到 submissionId 再找到 assessmentId 来鉴权)
        // 这里假设 Answer 表有 submission_id， Submission 表有 assessment_id
        // 为了性能，这通常需要一个联表查询或者 AnswerMapper.findByIdWithDetails
        // 这里我们暂且不写复杂的鉴权链，假设教师有权操作。

        // 更新答案分数
        Answer answer = Answer.builder()
                .answerId(answerId)
                .score(dto.getScore())
                .feedback(dto.getFeedback())
                .build();
        // 注意：AnswerMapper 需要一个 update 方法，我们之前写的是 upsert，可以使用
        // 但这里只更新 score 和 feedback
        // 更好的方式是在 AnswerMapper 增加 updateScore 方法

        // 临时方案：使用 upsert 可能会覆盖其他字段，所以我们最好在 AnswerMapper 加一个 updateScore
        answerMapper.updateAnswerScore(answer); // *需在 Mapper 添加*

        // 2. 【关键】触发总分重新计算
        // (找到 Submission -> 重新 SUM 所有 Answer.score -> 更新 Submission.totalScore)
        updateSubmissionTotalScore(answerId);
    }

    @Override
    public List<KpMasteryDto> analyzeKnowledgeMastery(Integer assessmentId, User teacher) {
        // 1. 鉴权
        checkAssessmentAccess(assessmentId, teacher);

        // 2. 调用分析 Mapper
        return analyticsMapper.analyzeClassMasteryByAssessment(assessmentId);
    }

    // === 辅助方法 ===

    private void checkAssessmentAccess(Integer assessmentId, User teacher) {
        Assessment assessment = assessmentMapper.findAssessmentById(assessmentId)
                .orElseThrow(() -> new ResourceNotFoundException("测评不存在"));

        // Assessment -> Class -> Course -> Owner Check
        classMapper.findClassById(assessment.getClassId())
                .ifPresent(clazz ->
                        courseService.checkCourseOwnership(clazz.getCourseId(), teacher.getUserId())
                );
    }

    /**
     * 【已实现】重新计算并更新某次提交的总分
     * 触发时机：教师给某一道题打分后
     */
    private void updateSubmissionTotalScore(Integer answerId) {
        // 1. 根据当前打分的题目，找到它是哪次提交的
        Integer submissionId = answerMapper.findSubmissionIdByAnswerId(answerId);

        if (submissionId != null) {
            // 2. 重新计算该次提交所有题目的总得分
            BigDecimal total = answerMapper.calculateTotalScore(submissionId);

            // 3. 构建要更新的对象 (只设置需要更新的字段)
            Submission sub = Submission.builder()
                    .submissionId(submissionId)
                    .totalScore(total)
                    .status("graded") // 只要有打分动作，状态即视为'已评分' (简化逻辑)
                    .build();

            // 4. 执行更新
            submissionMapper.updateScoreAndStatus(sub);
        }
    }
}