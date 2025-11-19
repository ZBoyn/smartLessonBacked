package com.neu.smartLesson.controller;

import com.neu.smartLesson.dto.GradeAnswerRequestDto;
import com.neu.smartLesson.dto.StudentSubmissionSummaryDto;
import com.neu.smartLesson.dto.analysis.KpMasteryDto;
import com.neu.smartLesson.model.User;
import com.neu.smartLesson.service.GradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher")
@PreAuthorize("hasRole('TEACHER')")
public class GradeController {

    @Autowired
    private GradeService gradeService;

    /**
     * 1. 查看某次测评的提交列表
     * GET /teacher/assessments/{assessmentId}/submissions
     */
    @GetMapping("/assessments/{assessmentId}/submissions")
    public ResponseEntity<List<StudentSubmissionSummaryDto>> getSubmissions(
            @PathVariable Integer assessmentId,
            @AuthenticationPrincipal User teacher) {

        return ResponseEntity.ok(gradeService.getSubmissionsForAssessment(assessmentId, teacher));
    }

    /**
     * 2. 教师人工打分 (针对某题)
     * PUT /teacher/answers/{answerId}/grade
     */
    @PutMapping("/answers/{answerId}/grade")
    public ResponseEntity<Void> gradeAnswer(
            @PathVariable Integer answerId,
            @RequestBody GradeAnswerRequestDto dto,
            @AuthenticationPrincipal User teacher) {

        gradeService.gradeAnswer(answerId, dto, teacher);
        return ResponseEntity.ok().build();
    }

    /**
     * 3. 【核心】学情分析：知识点掌握度
     * GET /teacher/assessments/{assessmentId}/analysis/knowledge-points
     */
    @GetMapping("/assessments/{assessmentId}/analysis/knowledge-points")
    public ResponseEntity<List<KpMasteryDto>> getKnowledgeAnalysis(
            @PathVariable Integer assessmentId,
            @AuthenticationPrincipal User teacher) {

        List<KpMasteryDto> analysis = gradeService.analyzeKnowledgeMastery(assessmentId, teacher);
        return ResponseEntity.ok(analysis);
    }
}